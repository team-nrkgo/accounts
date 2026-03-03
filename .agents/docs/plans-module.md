---
description: Plans & Subscriptions module architecture, rules, and design decisions for the NRKGO accounts service
---

# Plans & Subscriptions Module

## Overview

This module lives in `com.nrkgo.accounts.modules.plans` and is the central hub for managing all product plans and subscriptions across the NRKGO platform.

**Key Rule: Plans are ORG-scoped, not user-scoped.**

- A subscription belongs to an **organization**
- All users in the org share the org's plan and limits
- Individual users do NOT have their own plans
- `activated_by` on the subscription tracks WHICH user made the change (admin or paying member) — for audit only

---

## Product Registry

Products are identified by an integer `product_code`. There is **no `products` table** — the code is used directly as a column.

```java
// ProductCodes.java
public static final int SNAP_STEPS = 101;
// Add new products here:
// public static final int CENTILIO_PDF = 102;
```

When adding a new product:

1. Add constant to `ProductCodes.java`
2. Seed its plans in the product's own SQL file
3. Add a case in `PlanServiceFactory.java`
4. Create an isolated `{product}_usage` table in the product's schema

---

## Database Tables

### `plans` — Plan Definitions

Replaces old `.properties` files. One row per plan per product.

```sql
id, product_code, plan_name, plan_type, price, currency, features_json, status
```

**Rules:**

- `UNIQUE KEY (product_code, plan_name)` — prevents duplicate plans per product
- `features_json` stores all limits and feature flags as a JSON object
- `plan_type`: `1` = Free, `2` = Paid, `3` = Lifetime
- **Never modify a row's `features_json` and expect existing paid users to be affected** — they are protected by `locked_features_json` on their subscription
- To change limits for new users: update `features_json`
- To add a new plan: insert a new row — do NOT modify existing rows

**`features_json` example:**

```json
{
  "max_guides": 5,
  "cloud_storage": false,
  "export_allowed": false,
  "max_steps_per_guide": 20
}
```

- `-1` means **unlimited**
- `false` means **feature disabled**
- Keys are product-specific (SnapSteps keys differ from future products)

---

### `subscriptions` — Full Plan History

**This is a history table.** Multiple rows per user per product are expected and correct.

```sql
id, user_id, product_code, plan_id, previous_plan_id,
locked_features_json, activation_source, start_time, expiry_time,
status, created_by, created_time, modified_time
```

**Rules:**

- **Never update a subscription row to change plans.** Always expire old row (`status=0`) and insert a new row.
- `status`: `1` = Active, `0` = Expired (superseded), `2` = Cancelled by user
- To get current plan: `WHERE user_id=? AND product_code=? AND status=1 ORDER BY created_time DESC LIMIT 1`
- `previous_plan_id` → points to `plans.id` the user was on before. Forms a linked list of history.
- `expiry_time`: `-1` for lifetime/free, epoch milliseconds for trials
- `created_by`: Admin `user_id` if admin-activated, `NULL` for self-service or system

**`activation_source` values:**
| Value | Meaning |
|---|---|
| `system` | Auto-initialized on signup (e.g., free plan) |
| `payment` | User paid via payment gateway |
| `admin` | Admin granted via admin tool |
| `trial` | Trial period given |
| `promo` | Promo/coupon code applied |

---

## Grandfathering (Critical Rule)

When a user subscribes to a **paid, trial, admin, or promo** plan:
→ Copy `plans.features_json` into `subscriptions.locked_features_json` at that moment.

When reading limits for a user:

1. If `locked_features_json` is set → **use it** (paid user, protected from plan changes)
2. If `locked_features_json` is NULL → use live `plans.features_json` (free/system users)

This means:

- You can freely update `plans.features_json` for new subscribers
- Existing paid users keep their original limits forever
- Free plan users always get the current free limits (intentional — free plan is not grandfathered)

---

## Usage Tracking

Usage tracking is **isolated per product** — each product has its own usage table.

SnapSteps: `ss_usage` table in `snapsteps.sql`

```sql
id, user_id, guides_count, exports_used, reset_time
```

**Rules:**

- One row per user (`UNIQUE user_id`)
- Use **atomic SQL increments** with conditional checks to avoid race conditions:
  ```sql
  UPDATE ss_usage
  SET guides_count = guides_count + 1
  WHERE user_id = ? AND guides_count < ?  -- limit from plan
  -- If rows_affected = 0 → reject (user is at limit)
  ```
- `reset_time`: When periodic counters reset (e.g., monthly exports). Tied to `billing_interval` in `features_json`
- Future products: create `{product}_usage` table in their own schema file

---

## Factory Pattern

```java
// PlanServiceFactory.java
PlanService service = planServiceFactory.getInstance(ProductCodes.SNAP_STEPS);
service.initFreePlan(user);
service.getActiveSubscription(user);
service.canAccess(user, "cloud_storage");
service.getLimit(user, "max_guides");
service.switchPlan(user, planId);
service.cancelSubscription(user);  // falls back to free
```

For admin tool activations:

```java
DefaultPlanService svc = (DefaultPlanService) planServiceFactory.getInstance(101);
svc.switchPlan(user, planId, DefaultPlanService.SOURCE_ADMIN, adminUserId, trialExpiryMs);
```

---

## Key Design Decisions (Don't Change Without Understanding)

| Decision                                           | Why                                                                                                                |
| -------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------ |
| No `products` table                                | Avoided unnecessary join. `product_code INT` column is simpler.                                                    |
| `features_json` not a separate `plan_limits` table | Query pattern is always "load one user's limits" — never cross-plan queries. JSON is simpler with no joins needed. |
| History rows instead of upsert                     | Full audit trail of every plan change. Required for support, billing disputes, admin grants.                       |
| `locked_features_json` on subscription             | Grandfathering: existing paid users are unaffected when plan limits change for new users.                          |
| Isolated `ss_usage` per product                    | Avoids "god table". Each product meters its own usage. New products just add their own table.                      |
| Atomic SQL increments for usage                    | Prevents race conditions when two requests arrive simultaneously at a usage limit.                                 |
| `activation_source` column                         | Required for admin tool grant tracking, trial vs payment distinction, analytics.                                   |

---

## File Structure

```
src/main/resources/db/
├── accounts.sql       -- Core tables: users, roles, orgs, sessions
├── plans.sql          -- Plans & subscriptions tables (this module)
└── snapsteps.sql      -- SnapSteps-specific tables (ss_guides, ss_usage)

src/main/java/com/nrkgo/accounts/modules/plans/
├── controller/
│   └── PlanController.java        -- REST endpoints
├── model/
│   ├── Plan.java                  -- Plan definitions entity
│   └── Subscription.java          -- Subscription history entity
├── repository/
│   ├── PlanRepository.java
│   └── SubscriptionRepository.java
└── service/
    ├── PlanService.java           -- Interface
    ├── DefaultPlanService.java    -- Core implementation (extend for product overrides)
    ├── PlanServiceFactory.java    -- Factory: getInstance(productCode)
    └── ProductCodes.java          -- Product code constants registry
```

---

## REST API Endpoints

| Method | URL                                 | Purpose                                              |
| ------ | ----------------------------------- | ---------------------------------------------------- |
| `GET`  | `/api/plans/status?productCode=101` | Get user's active subscription                       |
| `GET`  | `/api/plans/list?productCode=101`   | List all available plans for a product               |
| `POST` | `/api/plans/init?productCode=101`   | Initialize free plan (called after signup)           |
| `POST` | `/api/plans/switch`                 | Switch to a new plan (body: `{planId, productCode}`) |
| `POST` | `/api/plans/cancel?productCode=101` | Cancel subscription → fall back to free              |

---

## How to Add a New Product (Checklist)

- [ ] Add `public static final int MY_PRODUCT = 102;` in `ProductCodes.java`
- [ ] Create `myproduct.sql` with the product's tables (e.g., `mp_usage`)
- [ ] Seed the product's plans in `myproduct.sql` (`INSERT IGNORE INTO plans ...`)
- [ ] Add a `case ProductCodes.MY_PRODUCT:` in `PlanServiceFactory.java`
- [ ] If the product needs custom plan logic, create `MyProductPlanService extends DefaultPlanService`
- [ ] Call `planServiceFactory.getInstance(102).initFreePlan(user)` after user signup for this product

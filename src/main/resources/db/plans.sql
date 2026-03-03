-- Plans & Subscriptions Module Schema
-- Plans belong to ORGANIZATIONS, not individual users.
-- All members of an org share the org's plan.

-- 1. Plans Table (Plan definitions)
CREATE TABLE IF NOT EXISTS plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code INT NOT NULL,                 -- 101: SnapSteps, 102: next product...
    plan_name VARCHAR(100) NOT NULL,
    plan_type INT NOT NULL,                    -- 1: Free, 2: Paid, 3: Lifetime
    price DECIMAL(10,2) DEFAULT 0.00,
    currency VARCHAR(10) DEFAULT 'USD',
    features_json LONGTEXT NOT NULL,           -- {"max_guides": 5, "cloud_storage": false}
    status INT DEFAULT 1,                      -- 1: Active, 0: Deprecated
    created_time BIGINT,
    modified_time BIGINT,
    INDEX (product_code),
    UNIQUE KEY uq_product_plan (product_code, plan_name)
);

-- 2. Subscriptions (Org-level plan history)
--    The ORG owns the subscription. All users in the org share this plan.
--    Multiple rows per org per product = full history.
--    Active = WHERE org_id=? AND product_code=? AND status=1 ORDER BY created_time DESC LIMIT 1
CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    org_id BIGINT NOT NULL,                    -- The org this plan belongs to
    product_code INT NOT NULL,
    plan_id BIGINT NOT NULL,
    previous_plan_id BIGINT,                   -- Linked history: what plan the org was on before

    -- Snapshot of plan limits at activation time (grandfathering protection).
    -- NULL for free/system-init → always uses live plans.features_json.
    -- SET for paid/admin/trial → locked in, protected from future plan changes.
    locked_features_json LONGTEXT,

    -- How this subscription was activated:
    -- 'system', 'payment', 'admin', 'trial', 'promo'
    activation_source VARCHAR(50) DEFAULT 'system',

    start_time BIGINT,
    expiry_time BIGINT,                        -- -1 for lifetime/free

    -- 1: Active, 0: Expired (superseded by a newer row), 2: Cancelled
    status INT DEFAULT 1,

    activated_by BIGINT,                       -- user_id of who activated (admin or org member who paid)
    created_time BIGINT,
    modified_time BIGINT,

    FOREIGN KEY (org_id) REFERENCES organizations(id),
    FOREIGN KEY (plan_id) REFERENCES plans(id),
    INDEX idx_active_sub (org_id, product_code, status)
);

-- Seed SnapSteps Plans (product_code = 101)
INSERT IGNORE INTO plans (product_code, plan_name, plan_type, price, features_json, status, created_time) VALUES
(101, 'Free',         1,  0.00, '{"max_guides": 5,  "cloud_storage": false, "export_allowed": false, "max_steps_per_guide": 20}', 1, UNIX_TIMESTAMP() * 1000),
(101, 'Pro',          2, 19.00, '{"max_guides": -1, "cloud_storage": true,  "export_allowed": true,  "max_steps_per_guide": -1}',  1, UNIX_TIMESTAMP() * 1000),
(101, 'Business LTD', 3, 49.00, '{"max_guides": -1, "cloud_storage": true,  "export_allowed": true,  "max_steps_per_guide": -1,  "custom_branding": true}', 1, UNIX_TIMESTAMP() * 1000);

package com.nrkgo.accounts.modules.plans.service;

import com.nrkgo.accounts.model.Organization;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.modules.plans.model.Subscription;

/**
 * Plan service interface — all methods operate at the ORG level.
 * Plans belong to organizations. All users in an org share the same plan.
 *
 * Usage:
 * PlanService svc = planServiceFactory.getInstance(ProductCodes.SNAP_STEPS);
 * svc.initFreePlan(org);
 * svc.canAccess(org, "cloud_storage");
 * svc.getLimit(org, "max_guides");
 */
public interface PlanService {

    /** Initialize the free plan for a newly created org. Called on org creation. */
    Subscription initFreePlan(Organization org, User activatedBy);

    /** Get the current active subscription for an org. */
    Subscription getActiveSubscription(Organization org);

    /** Check if the org's plan allows a specific feature (boolean flag). */
    boolean canAccess(Organization org, String featureKey);

    /**
     * Get the org's numeric limit for a feature key.
     * Returns -1 for unlimited, 0 if feature not found.
     */
    int getLimit(Organization org, String featureKey);

    /** Switch the org to a new plan. */
    Subscription switchPlan(Organization org, Long newPlanId, String activationSource, Long activatedByUserId,
            Long expiryTime);

    /** Cancel org's current plan and fall back to Free. */
    void cancelSubscription(Organization org, Long activatedByUserId);
}

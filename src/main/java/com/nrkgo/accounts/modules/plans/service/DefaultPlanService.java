package com.nrkgo.accounts.modules.plans.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nrkgo.accounts.model.Organization;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.modules.plans.model.Plan;
import com.nrkgo.accounts.modules.plans.model.Subscription;
import com.nrkgo.accounts.modules.plans.repository.PlanRepository;
import com.nrkgo.accounts.modules.plans.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class DefaultPlanService implements PlanService {

    protected static final int STATUS_ACTIVE = 1;
    protected static final int STATUS_EXPIRED = 0;
    protected static final int PLAN_TYPE_FREE = 1;

    public static final String SOURCE_SYSTEM = "system";
    public static final String SOURCE_PAYMENT = "payment";
    public static final String SOURCE_ADMIN = "admin";
    public static final String SOURCE_TRIAL = "trial";
    public static final String SOURCE_PROMO = "promo";

    protected final PlanRepository planRepository;
    protected final SubscriptionRepository subscriptionRepository;
    protected final ObjectMapper objectMapper;

    protected int productCode = ProductCodes.SNAP_STEPS;

    public DefaultPlanService(PlanRepository planRepository,
            SubscriptionRepository subscriptionRepository,
            ObjectMapper objectMapper) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.objectMapper = objectMapper;
    }

    protected Plan getFreePlan() {
        return planRepository.findByProductCodeAndStatus(productCode, STATUS_ACTIVE)
                .stream()
                .filter(p -> p.getPlanType() == PLAN_TYPE_FREE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No free plan for product: " + productCode));
    }

    @Override
    public Subscription getActiveSubscription(Organization org) {
        return subscriptionRepository
                .findFirstByOrgIdAndProductCodeAndStatusOrderByCreatedTimeDesc(
                        org.getId(), productCode, STATUS_ACTIVE)
                .orElse(null);
    }

    @Override
    @Transactional
    public Subscription initFreePlan(Organization org, User activatedBy) {
        // Idempotent: if org already has an active subscription, return it
        Subscription existing = getActiveSubscription(org);
        if (existing != null)
            return existing;

        Plan freePlan = getFreePlan();
        return createSubscription(org, freePlan, null, SOURCE_SYSTEM,
                activatedBy != null ? activatedBy.getId() : null, null);
    }

    @Override
    @Transactional
    public Subscription switchPlan(Organization org, Long newPlanId,
            String activationSource, Long activatedByUserId, Long expiryTime) {
        Plan newPlan = planRepository.findById(newPlanId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + newPlanId));

        // Get current for history chain
        Subscription currentSub = getActiveSubscription(org);
        Long previousPlanId = (currentSub != null) ? currentSub.getPlan().getId() : null;

        // Expire the current active subscription
        if (currentSub != null) {
            currentSub.setStatus(STATUS_EXPIRED);
            currentSub.setModifiedTime(System.currentTimeMillis());
            subscriptionRepository.save(currentSub);
        }

        return createSubscription(org, newPlan, previousPlanId, activationSource, activatedByUserId, expiryTime);
    }

    @Override
    @Transactional
    public void cancelSubscription(Organization org, Long activatedByUserId) {
        Plan freePlan = getFreePlan();
        switchPlan(org, freePlan.getId(), SOURCE_SYSTEM, activatedByUserId, null);
    }

    @Override
    public boolean canAccess(Organization org, String featureKey) {
        Subscription sub = getActiveSubscription(org);
        if (sub == null)
            return false;
        return readBoolean(getEffectiveFeaturesJson(sub), featureKey);
    }

    @Override
    public int getLimit(Organization org, String featureKey) {
        Subscription sub = getActiveSubscription(org);
        if (sub == null)
            return 0;
        return readInt(getEffectiveFeaturesJson(sub), featureKey);
    }

    // --- Private helpers ---

    private Subscription createSubscription(Organization org, Plan plan, Long previousPlanId,
            String activationSource, Long activatedByUserId, Long expiryTime) {
        long now = System.currentTimeMillis();

        Subscription sub = new Subscription();
        sub.setOrg(org);
        sub.setProductCode(productCode);
        sub.setPlan(plan);
        sub.setPreviousPlanId(previousPlanId);
        sub.setActivationSource(activationSource);
        sub.setActivatedBy(activatedByUserId);
        sub.setStatus(STATUS_ACTIVE);
        sub.setStartTime(now);
        sub.setExpiryTime(expiryTime != null ? expiryTime : -1L);
        sub.setCreatedTime(now);
        sub.setModifiedTime(now);

        // Lock limits for non-system activations (grandfathering)
        if (!SOURCE_SYSTEM.equals(activationSource)) {
            sub.setLockedFeaturesJson(plan.getFeaturesJson());
        }

        return subscriptionRepository.save(sub);
    }

    private String getEffectiveFeaturesJson(Subscription sub) {
        // Paid/admin/trial: use locked snapshot (grandfathered)
        if (sub.getLockedFeaturesJson() != null)
            return sub.getLockedFeaturesJson();
        // Free/system: use live plan limits
        return sub.getPlan().getFeaturesJson();
    }

    private boolean readBoolean(String json, String key) {
        try {
            JsonNode node = objectMapper.readTree(json).get(key);
            if (node == null)
                return false;
            if (node.isBoolean())
                return node.asBoolean();
            if (node.isNumber())
                return node.asInt() != 0;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private int readInt(String json, String key) {
        try {
            JsonNode node = objectMapper.readTree(json).get(key);
            return node != null ? node.asInt() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}

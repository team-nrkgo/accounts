package com.nrkgo.accounts.modules.plans.dto;

import com.nrkgo.accounts.dto.InitResponse;
import com.nrkgo.accounts.modules.plans.model.Plan;

/**
 * Extended init response for product-specific init endpoints ({product}/init).
 * Merges the standard InitResponse (user, orgs) with the org's plan info for
 * this product.
 */
public class ProductInitResponse {

    // ── From existing InitResponse ──────────────────────────────────────────
    private Object userInformation;
    private Object defaultOrganization;
    private Object otherOrganizations;

    // ── Product-specific plan info ──────────────────────────────────────────
    private PlanSummary plan;

    public static class PlanSummary {
        private String planName;
        private Integer planType; // 1: Free, 2: Paid, 3: Lifetime
        private Long expiryTime; // -1 for lifetime/free
        private String activationSource; // system/payment/admin/trial/promo
        private Object features; // Parsed features_json as a map

        public String getPlanName() {
            return planName;
        }

        public void setPlanName(String planName) {
            this.planName = planName;
        }

        public Integer getPlanType() {
            return planType;
        }

        public void setPlanType(Integer planType) {
            this.planType = planType;
        }

        public Long getExpiryTime() {
            return expiryTime;
        }

        public void setExpiryTime(Long expiryTime) {
            this.expiryTime = expiryTime;
        }

        public String getActivationSource() {
            return activationSource;
        }

        public void setActivationSource(String activationSource) {
            this.activationSource = activationSource;
        }

        public Object getFeatures() {
            return features;
        }

        public void setFeatures(Object features) {
            this.features = features;
        }
    }

    // Manual Getters & Setters
    public Object getUserInformation() {
        return userInformation;
    }

    public void setUserInformation(Object userInformation) {
        this.userInformation = userInformation;
    }

    public Object getDefaultOrganization() {
        return defaultOrganization;
    }

    public void setDefaultOrganization(Object defaultOrganization) {
        this.defaultOrganization = defaultOrganization;
    }

    public Object getOtherOrganizations() {
        return otherOrganizations;
    }

    public void setOtherOrganizations(Object otherOrganizations) {
        this.otherOrganizations = otherOrganizations;
    }

    public PlanSummary getPlan() {
        return plan;
    }

    public void setPlan(PlanSummary plan) {
        this.plan = plan;
    }
}

package com.nrkgo.accounts.modules.plans.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload for POST /admin/plans/assign
 *
 * Plans are org-scoped. All users in the org share the org's plan.
 *
 * Two behaviours:
 * 1. payload plan_name == org's current plan → update limits only
 * (custom_limits_json)
 * 2. payload plan_name != org's current plan → full plan switch
 */
public class AdminPlanRequest {

    @JsonProperty("org_id")
    private Long orgId;

    @JsonProperty("product_code")
    private Integer productCode;

    /**
     * Must exactly match a plan_name in the plans table. E.g. "Free", "Pro",
     * "Business LTD"
     */
    @JsonProperty("plan_name")
    private String planName;

    /**
     * Optional. Override the plan's default limits for this org only.
     * If null: uses the plan's standard features_json.
     * Example: "{\"max_guides\": 100, \"cloud_storage\": true}"
     */
    @JsonProperty("custom_limits_json")
    private String customLimitsJson;

    /** Optional. Expiry in epoch ms. Null or -1 = lifetime. */
    @JsonProperty("expiry_time")
    private Long expiryTime;

    /** Optional audit note. */
    @JsonProperty("note")
    private String note;

    // Getters and Setters
    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Integer getProductCode() {
        return productCode;
    }

    public void setProductCode(Integer productCode) {
        this.productCode = productCode;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getCustomLimitsJson() {
        return customLimitsJson;
    }

    public void setCustomLimitsJson(String customLimitsJson) {
        this.customLimitsJson = customLimitsJson;
    }

    public Long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

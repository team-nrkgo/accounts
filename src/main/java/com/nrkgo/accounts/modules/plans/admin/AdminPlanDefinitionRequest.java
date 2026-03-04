package com.nrkgo.accounts.modules.plans.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class AdminPlanDefinitionRequest {

    @JsonProperty("product_code")
    private Integer productCode;

    @JsonProperty("plan_name")
    private String planName;

    /** 1: Free, 2: Paid, 3: Lifetime */
    @JsonProperty("plan_type")
    private Integer planType;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("currency")
    private String currency;

    /**
     * Feature limits as a JSON string.
     * Example: {"max_guides": 5, "cloud_storage": false, "export_allowed": false}
     * -1 = unlimited, false = disabled
     */
    @JsonProperty("features_json")
    private String featuresJson;

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

    public Integer getPlanType() {
        return planType;
    }

    public void setPlanType(Integer planType) {
        this.planType = planType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getFeaturesJson() {
        return featuresJson;
    }

    public void setFeaturesJson(String featuresJson) {
        this.featuresJson = featuresJson;
    }
}

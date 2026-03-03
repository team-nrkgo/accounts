package com.nrkgo.accounts.modules.plans.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "plans", uniqueConstraints = @UniqueConstraint(name = "uq_product_plan", columnNames = { "product_code",
        "plan_name" }))
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", nullable = false)
    private Integer productCode;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(name = "plan_type", nullable = false)
    private Integer planType; // 1: Free, 2: Paid, 3: Lifetime

    private BigDecimal price;

    private String currency;

    @Column(name = "features_json", columnDefinition = "LONGTEXT", nullable = false)
    private String featuresJson;

    private Integer status; // 1: Active, 0: Deprecated

    @Column(name = "created_time")
    private Long createdTime;

    @Column(name = "modified_time")
    private Long modifiedTime;

    // --- Manual Getters & Setters (Lombok not active at compile time) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public Long getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}

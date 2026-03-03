package com.nrkgo.accounts.modules.plans.model;

import com.nrkgo.accounts.model.Organization;
import jakarta.persistence.*;

@Entity
@Table(name = "subscriptions", indexes = @Index(name = "idx_active_sub", columnList = "org_id, product_code, status"))
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization org;

    @Column(name = "product_code", nullable = false)
    private Integer productCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "previous_plan_id")
    private Long previousPlanId;

    @Column(name = "locked_features_json", columnDefinition = "LONGTEXT")
    private String lockedFeaturesJson;

    @Column(name = "activation_source")
    private String activationSource;

    @Column(name = "start_time")
    private Long startTime;

    @Column(name = "expiry_time")
    private Long expiryTime;

    private Integer status;

    @Column(name = "activated_by")
    private Long activatedBy;

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

    public Organization getOrg() {
        return org;
    }

    public void setOrg(Organization org) {
        this.org = org;
    }

    public Integer getProductCode() {
        return productCode;
    }

    public void setProductCode(Integer productCode) {
        this.productCode = productCode;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public Long getPreviousPlanId() {
        return previousPlanId;
    }

    public void setPreviousPlanId(Long previousPlanId) {
        this.previousPlanId = previousPlanId;
    }

    public String getLockedFeaturesJson() {
        return lockedFeaturesJson;
    }

    public void setLockedFeaturesJson(String lockedFeaturesJson) {
        this.lockedFeaturesJson = lockedFeaturesJson;
    }

    public String getActivationSource() {
        return activationSource;
    }

    public void setActivationSource(String activationSource) {
        this.activationSource = activationSource;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getActivatedBy() {
        return activatedBy;
    }

    public void setActivatedBy(Long activatedBy) {
        this.activatedBy = activatedBy;
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

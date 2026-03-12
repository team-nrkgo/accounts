package com.nrkgo.accounts.modules.scheduler.model;

import jakarta.persistence.*;

@Entity
@Table(name = "job_arguments", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "org_id", "product_code", "job_class", "entity_id" })
})
public class JobArguments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @Column(name = "product_code", nullable = false)
    private Integer productCode;

    @Column(name = "job_class", nullable = false)
    private String jobClass;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "arg_ref")
    private String argRef;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "job_type", nullable = false)
    private Integer jobType = 0; // 0: Single, 1: Repeated

    @Column(name = "json_data", columnDefinition = "TEXT")
    private String jsonData;

    @Column(name = "active_trigger_id")
    private Long activeTriggerId;

    @Column(name = "status")
    private Integer status = 0; // 0: Active, 1: Inactive

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_time")
    private Long createdTime;

    @Column(name = "modified_by")
    private Long modifiedBy;

    @Column(name = "modified_time")
    private Long modifiedTime;

    public JobArguments() {
    }

    @PrePersist
    protected void onCreate() {
        createdTime = System.currentTimeMillis();
        modifiedTime = createdTime;
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedTime = System.currentTimeMillis();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getJobClass() {
        return jobClass;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getArgRef() {
        return argRef;
    }

    public void setArgRef(String argRef) {
        this.argRef = argRef;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getJobType() {
        return jobType;
    }

    public void setJobType(Integer jobType) {
        this.jobType = jobType;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    public Long getActiveTriggerId() {
        return activeTriggerId;
    }

    public void setActiveTriggerId(Long activeTriggerId) {
        this.activeTriggerId = activeTriggerId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Long getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Long modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}

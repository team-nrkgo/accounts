package com.nrkgo.accounts.modules.scheduler.model;

import jakarta.persistence.*;

@Entity
@Table(name = "job_repeat_rules")
public class JobRepeatRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_args_id", nullable = false)
    private Long jobArgsId;

    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "repeat_type", nullable = false)
    private Integer repeatType; // 1: Daily, 2: Weekly, 3: Monthly

    @Column(name = "repeat_count")
    private Integer repeatCount = -1;

    @Column(name = "start_time")
    private Long startTime;

    @Column(name = "end_time")
    private Long endTime;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_time")
    private Long createdTime;

    @Column(name = "modified_by")
    private Long modifiedBy;

    @Column(name = "modified_time")
    private Long modifiedTime;

    public JobRepeatRule() {
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

    public Long getJobArgsId() {
        return jobArgsId;
    }

    public void setJobArgsId(Long jobArgsId) {
        this.jobArgsId = jobArgsId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Integer getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(Integer repeatType) {
        this.repeatType = repeatType;
    }

    public Integer getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(Integer repeatCount) {
        this.repeatCount = repeatCount;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
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

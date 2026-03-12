package com.nrkgo.accounts.modules.scheduler.model;

import jakarta.persistence.*;

@Entity
@Table(name = "job_triggers")
public class JobTrigger {

    public static final int WAIT = 0;
    public static final int RUNNING = 1;
    public static final int FINISHED = 2;
    public static final int FAILED = 3;
    public static final int CANCELLED = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_args_id", nullable = false)
    private Long jobArgsId;

    @Column(name = "start_time", nullable = false)
    private Long startTime;

    @Column(name = "status", nullable = false)
    private Integer status = WAIT;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "thread_pool")
    private String threadPool = "default";

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_time")
    private Long createdTime;

    @Column(name = "modified_by")
    private Long modifiedBy;

    @Column(name = "modified_time")
    private Long modifiedTime;

    public JobTrigger() {
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

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(String threadPool) {
        this.threadPool = threadPool;
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

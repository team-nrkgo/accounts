package com.nrkgo.accounts.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDateTime;

@MappedSuperclass
@Data
public abstract class BaseEntity {

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @LastModifiedBy
    @Column(name = "modified_by")
    private Long modifiedBy;

    @UpdateTimestamp
    @Column(name = "modified_time")
    private LocalDateTime modifiedTime;

    // Manual Accessors
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public Long getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(Long modifiedBy) { this.modifiedBy = modifiedBy; }

    public LocalDateTime getModifiedTime() { return modifiedTime; }
    public void setModifiedTime(LocalDateTime modifiedTime) { this.modifiedTime = modifiedTime; }
}

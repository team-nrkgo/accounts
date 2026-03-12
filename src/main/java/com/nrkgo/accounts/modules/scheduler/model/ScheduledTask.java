package com.nrkgo.accounts.modules.scheduler.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "scheduled_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledTask {

    public enum TaskStatus {
        PENDING, RUNNING, COMPLETED, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_type", nullable = false)
    private String taskType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "execution_time", nullable = false)
    private Long executionTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    private Integer maxRetries = 3;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "payload_json", columnDefinition = "TEXT")
    private String payloadJson;

    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "created_time")
    private Long createdTime;

    @Column(name = "modified_time")
    private Long modifiedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = System.currentTimeMillis();
        modifiedTime = createdTime;
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedTime = System.currentTimeMillis();
    }
}

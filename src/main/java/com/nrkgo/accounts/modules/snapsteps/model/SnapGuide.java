package com.nrkgo.accounts.modules.snapsteps.model;

import com.nrkgo.accounts.model.BaseEntity;
import com.nrkgo.accounts.model.User;
import jakarta.persistence.*;

@Entity
@Table(name = "ss_guides")
public class SnapGuide extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Auto-incrementing Primary Key

    @Column(name = "external_id", unique = true, nullable = false)
    private String externalId; // The ID from your extension (guide_17...)

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "steps_json", columnDefinition = "LONGTEXT")
    private String stepsJson;

    @Column(name = "total_steps")
    private Integer totalSteps;

    @Column(name = "first_url", columnDefinition = "TEXT")
    private String firstUrl;

    @Column(name = "storage_type")
    private String storageType;

    // Manual Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStepsJson() {
        return stepsJson;
    }

    public void setStepsJson(String stepsJson) {
        this.stepsJson = stepsJson;
    }

    public Integer getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }

    public String getFirstUrl() {
        return firstUrl;
    }

    public void setFirstUrl(String firstUrl) {
        this.firstUrl = firstUrl;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }
}

package com.nrkgo.accounts.modules.snapsteps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SnapGuideDto {

    @JsonProperty("external_id")
    private String externalId; // Explicitly renamed to match the concept

    private String title;

    private List<SnapStepDto> steps;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("storageType")
    private String storageType;

    // Getters and Setters
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

    public List<SnapStepDto> getSteps() {
        return steps;
    }

    public void setSteps(List<SnapStepDto> steps) {
        this.steps = steps;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }
}

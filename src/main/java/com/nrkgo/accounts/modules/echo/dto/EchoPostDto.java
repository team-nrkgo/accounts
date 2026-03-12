package com.nrkgo.accounts.modules.echo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.io.Serializable;
import java.util.List;

public class EchoPostDto implements Serializable {
    private Long id;

    @JsonProperty("external_id")
    @JsonAlias({ "externalId", "external_id" })
    private String externalId;

    @JsonProperty("org_id")
    @JsonAlias({ "orgId", "org_id", "org_Id" })
    private Long orgId;

    @JsonProperty("category_id")
    @JsonAlias({ "categoryId", "category_id" })
    private Long categoryId;

    private String title;
    private String slug;

    @JsonProperty("featured_image_url")
    @JsonAlias({ "featuredImageUrl", "featured_image_url" })
    private String featuredImageUrl;

    @JsonProperty("content_json")
    @JsonAlias({ "contentJson", "content_json" })
    private String contentJson;

    @JsonProperty("metadata_json")
    @JsonAlias({ "metadataJson", "metadata_json" })
    private String metadataJson;

    private String status;

    @JsonProperty("scheduled_time")
    @JsonAlias({ "scheduledTime", "scheduled_time" })
    private Long scheduledTime;

    @JsonProperty("created_time")
    @JsonAlias({ "createdTime", "created_time" })
    private Long createdTime;

    @JsonProperty("modified_time")
    @JsonAlias({ "modifiedTime", "modified_time" })
    private Long modifiedTime;

    private List<String> tags; // List of tag names

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

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getFeaturedImageUrl() {
        return featuredImageUrl;
    }

    public void setFeaturedImageUrl(String featuredImageUrl) {
        this.featuredImageUrl = featuredImageUrl;
    }

    public String getContentJson() {
        return contentJson;
    }

    public void setContentJson(String contentJson) {
        this.contentJson = contentJson;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Long scheduledTime) {
        this.scheduledTime = scheduledTime;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}

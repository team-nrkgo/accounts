package com.nrkgo.accounts.modules.plans.admin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AdminProductRequest {

    @JsonProperty("product_code")
    private Integer productCode;

    @JsonProperty("slug")
    private String slug; // URL path: "snapsteps" → /snapsteps/init

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    public Integer getProductCode() {
        return productCode;
    }

    public void setProductCode(Integer productCode) {
        this.productCode = productCode;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

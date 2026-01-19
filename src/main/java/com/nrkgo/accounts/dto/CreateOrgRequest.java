package com.nrkgo.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateOrgRequest {

    private Long orgId; // Optional for Create, Required for Update

    @NotBlank(message = "Organization Name is required")
    private String orgName;

    private String website;
    private String employeeCount;
    private String description;
    
    // Manual Accessors
    
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }

    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(String employeeCount) { this.employeeCount = employeeCount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

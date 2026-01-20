package com.nrkgo.accounts.dto;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateOrgRequest {

    @JsonProperty("org_id")
    private Long orgId; // Optional for Create, Required for Update

    @NotBlank(message = "Organization Name is required")
    @JsonProperty("org_name")
    private String orgName;

    private String website;
    
    @JsonProperty("employee_count")
    @jakarta.validation.constraints.Pattern(regexp = "\\d+", message = "Employee count must be a number")
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

package com.nrkgo.accounts.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "organizations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orgName;

    @Column(nullable = false)
    private String orgUrlName;

    private Integer orgType;

    private String website;

    private String employeeCount;

    private String mobile;
    
    private String description;

    private String applicationName;
    
    private Long appIconDark;
    
    private Long appIconLight;
    
    private Integer status; // 1: Active
    
    // Manual Accessors
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOrgName() { return orgName; }
    public void setOrgName(String orgName) { this.orgName = orgName; }
    
    public String getOrgUrlName() { return orgUrlName; }
    public void setOrgUrlName(String orgUrlName) { this.orgUrlName = orgUrlName; }
    
    public Integer getOrgType() { return orgType; }
    public void setOrgType(Integer orgType) { this.orgType = orgType; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    public String getEmployeeCount() { return employeeCount; }
    public void setEmployeeCount(String employeeCount) { this.employeeCount = employeeCount; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getApplicationName() { return applicationName; }
    public void setApplicationName(String applicationName) { this.applicationName = applicationName; }
    
    public Long getAppIconDark() { return appIconDark; }
    public void setAppIconDark(Long appIconDark) { this.appIconDark = appIconDark; }
    
    public Long getAppIconLight() { return appIconLight; }
    public void setAppIconLight(Long appIconLight) { this.appIconLight = appIconLight; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}

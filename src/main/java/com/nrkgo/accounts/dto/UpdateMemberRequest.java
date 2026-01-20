package com.nrkgo.accounts.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateMemberRequest {

    private Long id; // ID of the member (OrgUser ID or User ID? Ideally OrgUser ID, but let's see. User ID might be better for consistent lookup if passed from UI)
    // Actually, UI has member.id which is OrgUser ID in OrgMemberResponse? Let's check OrgMemberResponse

    @NotNull
    private Long memberId; // OrgUser ID

    @NotNull
    private Long orgId;

    private String firstName;
    private String lastName;
    private String designation;
    
    // Getters and Setters
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    
    private Long roleId;
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
}

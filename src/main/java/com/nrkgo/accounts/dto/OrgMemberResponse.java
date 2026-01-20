package com.nrkgo.accounts.dto;



public class OrgMemberResponse {
    private Long id; // OrgUser ID
    private String userEmail;
    private String roleName;
    private String designation;
    private String firstName;
    private String lastName;
    private Integer status; // 1: Active, 0: Pending
    private Long createdTime;
    private Long roleId;
    private String inviteToken;

    public OrgMemberResponse() {}

    public OrgMemberResponse(Long id, String userEmail, String roleName, String designation, String firstName, String lastName, Integer status, Long createdTime, Long roleId, String inviteToken) {
        this.id = id;
        this.userEmail = userEmail;
        this.roleName = roleName;
        this.designation = designation;
        this.firstName = firstName;
        this.lastName = lastName;
        this.status = status;
        this.createdTime = createdTime;
        this.roleId = roleId;
        this.inviteToken = inviteToken;
    }
    
    public String getInviteToken() { return inviteToken; }
    public void setInviteToken(String inviteToken) { this.inviteToken = inviteToken; }
    
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Long getCreatedTime() { return createdTime; }
    public void setCreatedTime(Long createdTime) { this.createdTime = createdTime; }
}

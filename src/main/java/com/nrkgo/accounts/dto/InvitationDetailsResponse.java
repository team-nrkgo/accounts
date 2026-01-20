package com.nrkgo.accounts.dto;

import lombok.Data;

@Data
public class InvitationDetailsResponse {
    private String email;
    private String orgName;
    private String firstName;
    private String lastName;
    private boolean newUser;

    public InvitationDetailsResponse(String email, String orgName, String firstName, String lastName, boolean newUser) {
        this.email = email;
        this.orgName = orgName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.newUser = newUser;
    }

    // Manual Accessors
    public String getEmail() { return email; }
    public String getOrgName() { return orgName; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public boolean isNewUser() { return newUser; }
}

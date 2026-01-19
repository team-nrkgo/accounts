package com.nrkgo.accounts.dto;

import com.nrkgo.accounts.model.Organization;
import com.nrkgo.accounts.model.User;
import lombok.Data;

import java.util.List;

@Data
public class InitResponse {

    private Organization defaultOrganizations;
    private List<Organization> otherOrganizations;
    private User userInformation;

    // Manual Accessors
    public Organization getDefaultOrganizations() { return defaultOrganizations; }
    public void setDefaultOrganizations(Organization defaultOrganizations) { this.defaultOrganizations = defaultOrganizations; }

    public List<Organization> getOtherOrganizations() { return otherOrganizations; }
    public void setOtherOrganizations(List<Organization> otherOrganizations) { this.otherOrganizations = otherOrganizations; }

    public User getUserInformation() { return userInformation; }
    public void setUserInformation(User userInformation) { this.userInformation = userInformation; }
}

package com.nrkgo.accounts.service;

import com.nrkgo.accounts.dto.InviteUserRequest;
import com.nrkgo.accounts.model.Digest;
import com.nrkgo.accounts.model.Organization;

import com.nrkgo.accounts.dto.CreateOrgRequest;

public interface OrgService {

    Organization createOrganization(CreateOrgRequest request, Long ownerId);
    
    Organization updateOrganization(CreateOrgRequest request, Long userId);

    Digest inviteUser(InviteUserRequest request, Long userId);

    void acceptInvite(String token, Long userId); // Authenticated user accepts token
}

package com.nrkgo.accounts.service;

import com.nrkgo.accounts.dto.InviteUserRequest;
import com.nrkgo.accounts.model.Digest;
import com.nrkgo.accounts.model.Organization;

public interface OrgService {

    Organization createOrganization(String name, Long ownerId);

    Digest inviteUser(Long orgId, InviteUserRequest request);

    void acceptInvite(String token, Long userId); // Authenticated user accepts token
}

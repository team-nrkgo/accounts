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

    java.util.List<com.nrkgo.accounts.dto.OrgMemberResponse> getOrgMembers(Long orgId, Long userId, String search);

    void updateMember(com.nrkgo.accounts.dto.UpdateMemberRequest request, Long requesterId);

    void removeMember(Long orgId, Long memberId, Long requesterId);

    // Roles
    java.util.List<com.nrkgo.accounts.model.Role> getOrgRoles(Long orgId);
    com.nrkgo.accounts.model.Role createOrgRole(com.nrkgo.accounts.dto.RoleRequest request, Long orgId, Long requesterId);
    com.nrkgo.accounts.model.Role updateOrgRole(Long roleId, com.nrkgo.accounts.dto.RoleRequest request, Long orgId, Long requesterId);
    void removeOrgRole(Long roleId, Long orgId, Long requesterId);
}

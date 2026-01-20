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

    void claimOrgAccess(Long orgId, Long userId); // Claim pending access by orgId

    com.nrkgo.accounts.dto.InvitationDetailsResponse getInvitationDetails(String token);

    com.nrkgo.accounts.model.UserSession createSessionFromInvite(String token, jakarta.servlet.http.HttpServletRequest httpRequest);

    String getOrGenerateInviteToken(Long requesterId, Long orgUserId);

    com.nrkgo.accounts.model.UserSession claimAccount(String token, String password, String firstName, String lastName, jakarta.servlet.http.HttpServletRequest httpRequest);

    java.util.List<com.nrkgo.accounts.dto.OrgMemberResponse> getOrgMembers(Long orgId, Long userId, String search);

    void updateMember(com.nrkgo.accounts.dto.UpdateMemberRequest request, Long requesterId);

    void removeMember(Long orgId, Long memberId, Long requesterId);

    // Roles
    java.util.List<com.nrkgo.accounts.model.Role> getOrgRoles(Long orgId);
    com.nrkgo.accounts.model.Role createOrgRole(com.nrkgo.accounts.dto.RoleRequest request, Long orgId, Long requesterId);
    com.nrkgo.accounts.model.Role updateOrgRole(Long roleId, com.nrkgo.accounts.dto.RoleRequest request, Long orgId, Long requesterId);
    void removeOrgRole(Long roleId, Long orgId, Long requesterId);
}

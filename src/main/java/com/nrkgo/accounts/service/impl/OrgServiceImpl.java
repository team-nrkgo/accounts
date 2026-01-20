package com.nrkgo.accounts.service.impl;

import com.nrkgo.accounts.dto.InviteUserRequest;
import com.nrkgo.accounts.model.Digest;
import com.nrkgo.accounts.model.OrgUser;
import com.nrkgo.accounts.model.Organization;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.repository.DigestRepository;
import com.nrkgo.accounts.repository.OrgUserRepository;
import com.nrkgo.accounts.repository.OrganizationRepository;
import com.nrkgo.accounts.repository.UserRepository;
import com.nrkgo.accounts.repository.RoleRepository;
import com.nrkgo.accounts.model.Role;
import com.nrkgo.accounts.service.OrgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OrgServiceImpl implements OrgService {

    private static final Logger log = LoggerFactory.getLogger(OrgServiceImpl.class);

    private final OrganizationRepository organizationRepository;
    private final OrgUserRepository orgUserRepository;
    private final DigestRepository digestRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    // Manual Constructor for DI
    public OrgServiceImpl(OrganizationRepository organizationRepository,
                          OrgUserRepository orgUserRepository,
                          DigestRepository digestRepository,
                          UserRepository userRepository,
                          RoleRepository roleRepository) {
        this.organizationRepository = organizationRepository;
        this.orgUserRepository = orgUserRepository;
        this.digestRepository = digestRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public Organization createOrganization(com.nrkgo.accounts.dto.CreateOrgRequest request, Long ownerId) {
        Organization org = new Organization();
        org.setOrgName(request.getOrgName());
        org.setOrgUrlName(request.getOrgName().toLowerCase().replaceAll("[^a-z0-9]", "-"));
        org.setStatus(1);
        org.setWebsite(request.getWebsite());
        org.setEmployeeCount(request.getEmployeeCount());
        org.setDescription(request.getDescription());
        
        // Audit Fields
        org.setCreatedBy(ownerId);
        org.setModifiedBy(ownerId);
        org.setCreatedTime(LocalDateTime.now());
        org.setModifiedTime(LocalDateTime.now());
        
        return organizationRepository.save(org);
    }

    @Override
    @Transactional
    public Organization updateOrganization(com.nrkgo.accounts.dto.CreateOrgRequest request, Long userId) {
        if (request.getOrgId() == null) {
            throw new IllegalArgumentException("Organization ID is required for update");
        }
        
        Organization org = organizationRepository.findById(request.getOrgId())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
        
        // TODO: Add permission check (Is userId an Admin of this org?)
        
        org.setOrgName(request.getOrgName());
        // We typically don't update URL Name to avoid breaking links, or we do it carefully. Keeping it same for now.
        org.setWebsite(request.getWebsite());
        org.setEmployeeCount(request.getEmployeeCount());
        org.setDescription(request.getDescription());
        
        // Audit Fields
        org.setModifiedBy(userId);
        org.setModifiedTime(LocalDateTime.now());
        
        return organizationRepository.save(org);
    }

     
     @Override
    @Transactional
    public Digest inviteUser(InviteUserRequest request, Long inviterId) {
        Long orgId = request.getOrgId();
        
        // Validate Role
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Role ID"));
            if (role.getOrgId() != null && !role.getOrgId().equals(orgId)) {
                throw new IllegalArgumentException("Role does not belong to this organization");
            }
        }
        
        // 1. Check if user exists
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            // Create Shadow User
            user = new User();
            user.setEmail(request.getEmail());
            // Save Names if provided for new user
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());

            user.setPassword("shadow-user-placeholder"); 
            user.setStatus(0); // Created/Pending
            // Audit Fields for Shadow User
            user.setCreatedBy(inviterId);
            user.setModifiedBy(inviterId);
            user.setCreatedTime(LocalDateTime.now());
            user.setModifiedTime(LocalDateTime.now());
            
            user = userRepository.save(user);
        }

        // 2. Check if OrgUser entry exists
        if (orgUserRepository.existsByOrgIdAndUserId(orgId, user.getId())) {
             throw new IllegalArgumentException("User is already a member or invited");
        }

        // 3. Create OrgUser (Pending)
        OrgUser orgUser = new OrgUser();
        orgUser.setOrgId(orgId);
        orgUser.setUserId(user.getId());
        orgUser.setRoleId(request.getRoleId());
        orgUser.setStatus(0); // Pending/Invited
        orgUser.setIsDefault(0);
        orgUser.setDesignation(request.getDesignation()); // Save Designation
        
        // Audit Fields
        orgUser.setCreatedBy(inviterId);
        orgUser.setModifiedBy(inviterId);
        orgUser.setCreatedTime(LocalDateTime.now());
        orgUser.setModifiedTime(LocalDateTime.now());
        
        orgUserRepository.save(orgUser);

        // 4. Create Digest (Token)
        String token = com.nrkgo.accounts.common.util.TokenUtils.generateToken();
        Digest digest = new Digest();
        digest.setEntityType("INVITE");
        digest.setEntityId(String.valueOf(orgUser.getId())); // Storing OrgUser ID reference
        digest.setToken(token);
        digest.setExpiryTime(LocalDateTime.now().plusDays(7));
        digest.setMetadata("email=" + request.getEmail());
        
        // Audit Fields
        digest.setCreatedBy(inviterId);
        digest.setModifiedBy(inviterId);
        digest.setCreatedTime(LocalDateTime.now());
        digest.setModifiedTime(LocalDateTime.now());
        
        return digestRepository.save(digest);
    }
    
    // ... (acceptInvite, getOrgMembers omitted for brevity in tool replacement if not modifying them, but I need to reach updateMember)
    // Wait, replacing chunks. I should use MultiReplace or just replace separate chunks.
    // I will use replace_file_content for inviteUser first.
    // Then another call for updateMember to avoid context matching issues with large chunks.
    
    // Actually I'll split it. This call is for inviteUser.

    @Override
    @Transactional
    public void acceptInvite(String token, Long userId) {
        Digest digest = digestRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));

        if (digest.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invitation expired");
        }

        if (!"INVITE".equals(digest.getEntityType())) {
            throw new IllegalArgumentException("Invalid token type");
        }

        // Parse Entity ID (OrgUser ID)
        Long orgUserId = Long.parseLong(digest.getEntityId());
        
        OrgUser orgUser = orgUserRepository.findById(orgUserId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation record not found"));

        if (orgUser.getStatus() != 0) {
             throw new IllegalArgumentException("Invitation already accepted or invalid");
        }

        // Verify User Match (Optional security check: does userId match the invited user?)
        // Assuming passed userId is the logged in user who clicked the link.
        // If it was a shadow user, we might need to merge or link them.
        
        // Update Status
        orgUser.setStatus(1); // Active
        orgUserRepository.save(orgUser);

        // Consume Token (Delete or Mark used)
        digestRepository.delete(digest); 
    }

    @Override
    public java.util.List<com.nrkgo.accounts.dto.OrgMemberResponse> getOrgMembers(Long orgId, Long userId, String search) {
        // ... (existing code, keeping it here for context if needed, but tool replaces contiguous block)
        // actually tool replaces block. I need to be careful. I will just append the new methods effectively.
        // Wait, replace_file_content replaces a block.
        // I will target the end of the file or just after getOrgMembers.
        
        // 1. Check if requester is a member of the org
        if (!orgUserRepository.existsByOrgIdAndUserId(orgId, userId)) {
            throw new IllegalArgumentException("Access denied: You are not a member of this organization");
        }
        
        // 2. Fetch members (with search if provided)
        if (search != null && !search.trim().isEmpty()) {
            return orgUserRepository.findMembersByOrgIdAndSearch(orgId, search.trim());
        } else {
            return orgUserRepository.findMembersByOrgId(orgId);
        }
    }

    @Override
    @Transactional
    public void updateMember(com.nrkgo.accounts.dto.UpdateMemberRequest request, Long requesterId) {
        // 1. Verify Requester
        OrgUser requester = orgUserRepository.findByOrgIdAndUserId(request.getOrgId(), requesterId)
                .orElseThrow(() -> new IllegalArgumentException("Access denied"));
        
        // 2. Find Target Member (OrgUser)
        OrgUser targetOrgUser = orgUserRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (!targetOrgUser.getOrgId().equals(request.getOrgId())) {
             throw new IllegalArgumentException("Member does not belong to this organization");
        }

        // 3. Update Org Data (Designation & Role)
        if (request.getDesignation() != null) {
            targetOrgUser.setDesignation(request.getDesignation());
        }
        
        if (request.getRoleId() != null) {
            // Validate Role
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Role ID"));
            if (role.getOrgId() != null && !role.getOrgId().equals(request.getOrgId())) {
                throw new IllegalArgumentException("Role does not belong to this organization");
            }
            
            // Allow role change
            targetOrgUser.setRoleId(request.getRoleId());
        }
        
        targetOrgUser.setModifiedBy(requesterId);
        targetOrgUser.setModifiedTime(LocalDateTime.now());
        orgUserRepository.save(targetOrgUser);

        // 4. Update User Data (Name)
        User targetUser = userRepository.findById(targetOrgUser.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (request.getFirstName() != null) targetUser.setFirstName(request.getFirstName());
        if (request.getLastName() != null) targetUser.setLastName(request.getLastName());
        
        userRepository.save(targetUser);
    }

    @Override
    @Transactional
    public void removeMember(Long orgId, Long memberId, Long requesterId) {
         // 1. Verify Requester
        if (!orgUserRepository.existsByOrgIdAndUserId(orgId, requesterId)) {
            throw new IllegalArgumentException("Access denied");
        }
        
        // 2. Find Target
        OrgUser targetOrgUser = orgUserRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (!targetOrgUser.getOrgId().equals(orgId)) {
             throw new IllegalArgumentException("Member does not belong to this organization");
        }

        // 3. Super Admin Guard
        Role role = roleRepository.findById(targetOrgUser.getRoleId()).orElse(null);
        if (role != null && (role.getName().equalsIgnoreCase("Admin") || role.getName().equalsIgnoreCase("Super Admin"))) {
             long adminCount = orgUserRepository.countByOrgIdAndRoleId(orgId, role.getId());
             if (adminCount <= 1) {
                 throw new IllegalArgumentException("Organization must have at least one " + role.getName());
             }
        }
        
        // 4. Delete
        orgUserRepository.delete(targetOrgUser);
    }

    // --- Role Management ---

    @Override
    public java.util.List<Role> getOrgRoles(Long orgId) {
        return roleRepository.findAllGlobalAndOrgRoles(orgId);
    }

    @Override
    @Transactional
    public Role createOrgRole(com.nrkgo.accounts.dto.RoleRequest request, Long orgId, Long requesterId) {
        // 1. Verify Permission (Requester must be Admin/Super Admin)
        if (!orgUserRepository.existsByOrgIdAndUserId(orgId, requesterId)) {
             throw new IllegalArgumentException("Access denied");
        }
        
        // 2. Check duplicate name in this Org
        if (roleRepository.existsByNameAndOrgId(request.getName(), orgId)) {
             throw new IllegalArgumentException("Role with this name already exists in organization");
        }

        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setOrgId(orgId);
        
        // Audit
        role.setCreatedBy(requesterId);
        role.setModifiedBy(requesterId);
        role.setCreatedTime(LocalDateTime.now());
        role.setModifiedTime(LocalDateTime.now());
        
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public Role updateOrgRole(Long roleId, com.nrkgo.accounts.dto.RoleRequest request, Long orgId, Long requesterId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        if (role.getOrgId() == null) {
            throw new IllegalArgumentException("Cannot edit system roles");
        }
        if (!role.getOrgId().equals(orgId)) {
            throw new IllegalArgumentException("Role does not belong to this organization");
        }
        
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setModifiedBy(requesterId);
        role.setModifiedTime(LocalDateTime.now());
        
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public void removeOrgRole(Long roleId, Long orgId, Long requesterId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        if (role.getOrgId() == null) {
            throw new IllegalArgumentException("Cannot delete system roles");
        }
        if (!role.getOrgId().equals(orgId)) {
            throw new IllegalArgumentException("Role does not belong to this organization");
        }
        
        long usageCount = orgUserRepository.countByOrgIdAndRoleId(orgId, roleId);
        if (usageCount > 0) {
            throw new IllegalArgumentException("Cannot delete role: It is assigned to " + usageCount + " users.");
        }

        roleRepository.delete(role);
    }
}

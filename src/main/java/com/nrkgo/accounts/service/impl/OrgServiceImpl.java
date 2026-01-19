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

    // Manual Constructor for DI
    public OrgServiceImpl(OrganizationRepository organizationRepository,
                          OrgUserRepository orgUserRepository,
                          DigestRepository digestRepository,
                          UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.orgUserRepository = orgUserRepository;
        this.digestRepository = digestRepository;
        this.userRepository = userRepository;
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
        
        // 1. Check if user exists
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            // Create Shadow User
            user = new User();
            user.setEmail(request.getEmail());
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
}

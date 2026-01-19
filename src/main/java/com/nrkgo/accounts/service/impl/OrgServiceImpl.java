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
    public Organization createOrganization(String name, Long ownerId) {
        // Standard Java creation
        Organization org = new Organization();
        org.setOrgName(name);
        org.setOrgUrlName(name.toLowerCase().replace(" ", "-")); // Simple slug generation
        org.setStatus(1);
        
        return organizationRepository.save(org);
    }

    @Override
    @Transactional
    public Digest inviteUser(Long orgId, InviteUserRequest request) {
        // 1. Check if user exists, if not create Shadow User? 
        // For simplicity, let's assume we find by email or create a shadow user.
        
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            // Create Shadow User
            user = new User();
            user.setEmail(request.getEmail());
            user.setPassword("shadow-user-placeholder"); // Should be unusable
            user.setStatus(0); // Created/Pending
            
            user = userRepository.save(user);
        }

        // 2. Check if OrgUser entry exists
        if (orgUserRepository.existsByOrgIdAndUserId(orgId, user.getId())) {
             // If status is active, throw error. If pending, maybe resend invite?
             // Simplification: throw error
             throw new IllegalArgumentException("User is already a member or invited");
        }

        // 3. Create OrgUser (Pending)
        OrgUser orgUser = new OrgUser();
        orgUser.setOrgId(orgId);
        orgUser.setUserId(user.getId());
        orgUser.setRoleId(request.getRoleId());
        orgUser.setStatus(0); // Pending/Invited
        
        orgUserRepository.save(orgUser);

        // 4. Create Digest (Token)
        String token = com.nrkgo.accounts.common.util.TokenUtils.generateToken();
        Digest digest = new Digest();
        digest.setEntityType("INVITE");
        digest.setEntityId(String.valueOf(orgUser.getId())); // Storing OrgUser ID reference
        digest.setToken(token);
        digest.setExpiryTime(LocalDateTime.now().plusDays(7));
        digest.setMetadata("email=" + request.getEmail());
        
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

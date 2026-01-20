package com.nrkgo.accounts.service.impl;

import com.nrkgo.accounts.dto.LoginRequest;
import com.nrkgo.accounts.dto.SignupRequest;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.model.UserSession;
import com.nrkgo.accounts.repository.UserRepository;
import com.nrkgo.accounts.repository.UserSessionRepository;
import com.nrkgo.accounts.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    
    private final com.nrkgo.accounts.repository.OrganizationRepository organizationRepository;
    private final com.nrkgo.accounts.repository.OrgUserRepository orgUserRepository;
    private final com.nrkgo.accounts.repository.RoleRepository roleRepository;

    // Manual Constructor for DI
    public UserServiceImpl(UserRepository userRepository, 
                           UserSessionRepository userSessionRepository, 
                           PasswordEncoder passwordEncoder,
                           com.nrkgo.accounts.repository.OrganizationRepository organizationRepository,
                           com.nrkgo.accounts.repository.OrgUserRepository orgUserRepository,
                           com.nrkgo.accounts.repository.RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizationRepository = organizationRepository;
        this.orgUserRepository = orgUserRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public User registerUser(SignupRequest request) {
        log.info("Registering user: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        // Using standard Java object creation + Setters instead of Builder
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Logic to derive firstName if missing
        String fName = request.getFirstName();
        if (fName == null || fName.trim().isEmpty()) {
             String email = request.getEmail();
             if (email != null && email.contains("@")) {
                 fName = email.split("@")[0];
             } else {
                 fName = "User"; // Fallback
             }
        }
        user.setFirstName(fName);
        
        user.setLastName(request.getLastName());
        user.setMobileNumber(request.getMobileNumber());
        
        // Logic to default TimeZone
        String tZone = request.getTimeZone();
        if (tZone == null || tZone.trim().isEmpty()) {
            tZone = "UTC";
        }
        user.setTimeZone(tZone);
        
        user.setCountry(request.getCountry());
        user.setStatus(0); // Default status (e.g., Created / Pending Verification)
        user.setSource(1); // Direct
        
        // Audit Fields for User (Self-referencing usually impossible before ID, but can update after save if strict needed)
        // For now, let's focus on the related entities which utilize the new User ID.

        User savedUser = userRepository.save(user);
        
        // Update User's own audit fields now that we have an ID
        savedUser.setCreatedBy(savedUser.getId());
        savedUser.setModifiedBy(savedUser.getId());
        savedUser.setCreatedTime(System.currentTimeMillis());
        savedUser.setModifiedTime(System.currentTimeMillis());
        userRepository.save(savedUser);

        // --- Default Organization Setup ---
        
        // 1. Create Default Organization
        com.nrkgo.accounts.model.Organization org = new com.nrkgo.accounts.model.Organization();
        String orgName = savedUser.getFirstName() + "'s Workspace";
        org.setOrgName(orgName);
        org.setOrgUrlName(orgName.toLowerCase().replaceAll("[^a-z0-9]", "-")); // Basic slugify
        org.setStatus(1); // Active
        
        // Audit Fields
        org.setCreatedBy(savedUser.getId());
        org.setModifiedBy(savedUser.getId());
        org.setCreatedTime(System.currentTimeMillis());
        org.setModifiedTime(System.currentTimeMillis());
        
        com.nrkgo.accounts.model.Organization savedOrg = organizationRepository.save(org);

        // 2. Resolve 'Super Admin' Role
        com.nrkgo.accounts.model.Role superAdminRole = roleRepository.findByName("Super Admin")
                .orElseGet(() -> {
                    // Auto-create if not exists
                    com.nrkgo.accounts.model.Role newRole = new com.nrkgo.accounts.model.Role();
                    newRole.setName("Super Admin");
                    newRole.setDescription("Default super admin role");
                    
                    // Audit Fields (Assigned to the first user claiming it)
                    newRole.setCreatedBy(savedUser.getId());
                    newRole.setModifiedBy(savedUser.getId());
                    newRole.setCreatedTime(System.currentTimeMillis());
                    newRole.setModifiedTime(System.currentTimeMillis());
                    
                    return roleRepository.save(newRole);
                });

        // 3. Add User to Org as Super Admin
        com.nrkgo.accounts.model.OrgUser orgUser = new com.nrkgo.accounts.model.OrgUser();
        orgUser.setOrgId(savedOrg.getId());
        orgUser.setUserId(savedUser.getId());
        orgUser.setRoleId(superAdminRole.getId());
        orgUser.setStatus(1); // Active
        orgUser.setIsDefault(1); // Default Org
        
        // Audit Fields
        orgUser.setCreatedBy(savedUser.getId());
        orgUser.setModifiedBy(savedUser.getId());
        orgUser.setCreatedTime(System.currentTimeMillis());
        orgUser.setModifiedTime(System.currentTimeMillis());
        
        orgUserRepository.save(orgUser);

        return savedUser;
    }

    @Override
    @Transactional
    public UserSession loginUser(LoginRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        log.info("Login attempt for: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return createSession(user, httpRequest);
    }

    @Override
    @Transactional
    public UserSession createSession(User user, jakarta.servlet.http.HttpServletRequest httpRequest) {
        // Create User Session
        String token = com.nrkgo.accounts.common.util.TokenUtils.generateToken(); // Secure Token
        
        // Using standard Java object creation + Setters instead of Builder
        UserSession session = new UserSession();
        session.setUserId(user.getId());
        session.setStatus(1); // Active
        session.setCookie(token);
        session.setExpireTime(System.currentTimeMillis() + 86400000L); // 1 day expiry

        if (httpRequest != null) {
            String userAgent = httpRequest.getHeader("User-Agent");
            session.setMachineIp(com.nrkgo.accounts.common.util.DeviceUtil.getClientIp(httpRequest));
            session.setBrowser(com.nrkgo.accounts.common.util.DeviceUtil.getBrowser(userAgent));
            session.setDeviceOs(com.nrkgo.accounts.common.util.DeviceUtil.getOs(userAgent));
            session.setDeviceName(com.nrkgo.accounts.common.util.DeviceUtil.getDeviceName(userAgent));
        }

        return userSessionRepository.save(session);
    }

    @Override
    public Integer checkUserStatus(String email) {
        return userRepository.findByEmail(email)
                .map(User::getStatus)
                .orElse(null);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean validateSession(String token) {
        return userSessionRepository.findByCookie(token)
                .map(session -> {
                    // Check if session is active (status=1) and not expired
                    if (session.getStatus() != 1) return false;
                    return session.getExpireTime() > System.currentTimeMillis();
                })
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public com.nrkgo.accounts.dto.InitResponse getInitData(Long userId, Long requestOrgId) {
        // 1. Fetch User Info
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2. Fetch all OrgUser records
        java.util.List<com.nrkgo.accounts.model.OrgUser> orgUsers = orgUserRepository.findByUserId(userId);
        
        if (orgUsers.isEmpty()) {
            throw new IllegalArgumentException("User does not belong to any organization");
        }

        // 3. Collect Org IDs and Map to Orgs
        java.util.List<Long> orgIds = orgUsers.stream()
                .map(com.nrkgo.accounts.model.OrgUser::getOrgId)
                .collect(java.util.stream.Collectors.toList());
        
        // Fetch all Organizations in one query
        java.util.List<com.nrkgo.accounts.model.Organization> allOrgs = organizationRepository.findAllById(orgIds);
        
        // Map ID -> Org for easy lookup
        java.util.Map<Long, com.nrkgo.accounts.model.Organization> orgMap = allOrgs.stream()
                .collect(java.util.stream.Collectors.toMap(com.nrkgo.accounts.model.Organization::getId, org -> org));

        // 4. Determine Default Org ID
        Long targetOrgId = null;

        if (requestOrgId != null) {
            // Priority 1: Requested explicitly
            targetOrgId = requestOrgId;
            // Verify user belongs to this org
            boolean belongs = orgUsers.stream().anyMatch(ou -> ou.getOrgId().equals(requestOrgId));
            if (!belongs) {
                 throw new SecurityException("User does not have access to requested organization");
            }
        } else {
            // Priority 2: 'is_default' flag in DB
            targetOrgId = orgUsers.stream()
                    .filter(ou -> ou.getIsDefault() != null && ou.getIsDefault() == 1)
                    .map(com.nrkgo.accounts.model.OrgUser::getOrgId)
                    .findFirst()
                    .orElse(null);
            
            // Priority 3: Fallback to first found
            if (targetOrgId == null && !orgIds.isEmpty()) {
                targetOrgId = orgIds.get(0);
            }
        }
        
        final Long finalDefaultId = targetOrgId;

        // 5. Build Response
        com.nrkgo.accounts.dto.InitResponse response = new com.nrkgo.accounts.dto.InitResponse();
        response.setUserInformation(user);
        
        java.util.List<com.nrkgo.accounts.model.Organization> otherOrgs = new java.util.ArrayList<>();
        
        for (com.nrkgo.accounts.model.Organization org : allOrgs) {
            if (org.getId().equals(finalDefaultId)) {
                response.setDefaultOrganizations(org);
            } else {
                otherOrgs.add(org);
            }
        }
        
        response.setOtherOrganizations(otherOrgs);
        
        return response;
    }
     

    @Override
    @Transactional(readOnly = true)
    public User getUserBySession(String token) {
        return userSessionRepository.findByCookie(token)
                .filter(session -> session.getStatus() == 1 && session.getExpireTime() > System.currentTimeMillis())
                .map(session -> userRepository.findById(session.getUserId()).orElse(null))
                .orElse(null);
    }

    @Override
    @Transactional
    public User updateUser(Long userId, com.nrkgo.accounts.dto.UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean updated = false;

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
            updated = true;
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
            updated = true;
        }
        if (request.getMobileNumber() != null) {
            user.setMobileNumber(request.getMobileNumber());
            updated = true;
        }
        if (request.getCountry() != null) {
            user.setCountry(request.getCountry());
            updated = true;
        }
        if (request.getTimeZone() != null) {
            user.setTimeZone(request.getTimeZone());
            updated = true;
        }

        if (updated) {
            user.setModifiedBy(userId);
            user.setModifiedTime(System.currentTimeMillis());
            return userRepository.save(user);
        }

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<UserSession> getUserSessions(Long userId) {
        return userSessionRepository.findByUserIdAndStatus(userId, 1).stream()
                .filter(s -> s.getExpireTime() > System.currentTimeMillis())
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public void revokeSession(Long sessionId, Long userId) {
        UserSession session = userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        
        if (!session.getUserId().equals(userId)) {
            throw new SecurityException("Not authorized to revoke this session");
        }
        
        session.setStatus(0); // Revoke
        session.setModifiedBy(userId);
        session.setModifiedTime(System.currentTimeMillis());
        userSessionRepository.save(session);
    }
}

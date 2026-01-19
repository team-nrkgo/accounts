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

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;

    // Manual Constructor for DI
    public UserServiceImpl(UserRepository userRepository, 
                           UserSessionRepository userSessionRepository, 
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.passwordEncoder = passwordEncoder;
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

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public UserSession loginUser(LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return createSession(user);
    }

    @Override
    @Transactional
    public UserSession createSession(User user) {
        // Create User Session
        String token = com.nrkgo.accounts.common.util.TokenUtils.generateToken(); // Secure Token
        
        // Using standard Java object creation + Setters instead of Builder
        UserSession session = new UserSession();
        session.setUserId(user.getId());
        session.setStatus(1); // Active
        session.setCookie(token);
        session.setExpireTime(LocalDateTime.now().plusDays(1)); // 1 day expiry

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
                    return session.getExpireTime().isAfter(LocalDateTime.now());
                })
                .orElse(false);
    }
}

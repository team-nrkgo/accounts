package com.nrkgo.accounts.service;

import com.nrkgo.accounts.dto.LoginRequest;
import com.nrkgo.accounts.dto.SignupRequest;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.model.UserSession;

import java.util.Optional;

public interface UserService {
    

    
    User registerUser(SignupRequest request);
    
    UserSession loginUser(LoginRequest request, jakarta.servlet.http.HttpServletRequest httpRequest);
    
    Integer checkUserStatus(String email);
    
    Optional<User> findByEmail(String email);

    boolean validateSession(String token);

    UserSession createSession(User user, jakarta.servlet.http.HttpServletRequest httpRequest);
    
    com.nrkgo.accounts.dto.InitResponse getInitData(Long userId, Long requestOrgId);
    
    User getUserBySession(String token);
    
    User updateUser(Long userId, com.nrkgo.accounts.dto.UpdateUserRequest request);

    java.util.List<UserSession> getUserSessions(Long userId);
    
    void revokeSession(Long sessionId, Long userId);
    
    com.nrkgo.accounts.model.User verifyUser(String token);
    
    void resendVerificationEmail(String email);

    void initiatePasswordReset(String email);

    void resetPassword(String token, String newPassword);
}

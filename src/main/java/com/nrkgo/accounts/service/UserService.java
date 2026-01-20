package com.nrkgo.accounts.service;

import com.nrkgo.accounts.dto.LoginRequest;
import com.nrkgo.accounts.dto.SignupRequest;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.model.UserSession;

import java.util.Optional;

public interface UserService {
    

    
    User registerUser(SignupRequest request);
    
    UserSession loginUser(LoginRequest request);
    
    Integer checkUserStatus(String email);
    
    Optional<User> findByEmail(String email);

    boolean validateSession(String token);

    UserSession createSession(User user);
    
    com.nrkgo.accounts.dto.InitResponse getInitData(Long userId, Long requestOrgId);
    
    User getUserBySession(String token);
    
    User updateUser(Long userId, com.nrkgo.accounts.dto.UpdateUserRequest request);
}

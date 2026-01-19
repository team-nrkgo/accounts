package com.nrkgo.accounts.repository;

import com.nrkgo.accounts.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByCookie(String cookie);
}

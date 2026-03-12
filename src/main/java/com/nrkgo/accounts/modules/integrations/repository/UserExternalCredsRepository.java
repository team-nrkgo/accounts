package com.nrkgo.accounts.modules.integrations.repository;

import com.nrkgo.accounts.modules.integrations.model.ExternalAccount;
import com.nrkgo.accounts.modules.integrations.model.UserExternalCreds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserExternalCredsRepository extends JpaRepository<UserExternalCreds, Long> {

    // Find the vault for a specific user and account identity
    Optional<UserExternalCreds> findByUserIdAndExternalAccount(Long userId, ExternalAccount externalAccount);

    List<UserExternalCreds> findByUserId(Long userId);

    // For background token refresh jobs
    List<UserExternalCreds> findByAuthTypeAndExpiryTimeLessThanEqual(
            com.nrkgo.accounts.modules.integrations.model.AuthType authType, Long expiryTime);
}

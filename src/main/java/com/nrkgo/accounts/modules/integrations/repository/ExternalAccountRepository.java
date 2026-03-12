package com.nrkgo.accounts.modules.integrations.repository;

import com.nrkgo.accounts.modules.integrations.model.ExternalAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExternalAccountRepository extends JpaRepository<ExternalAccount, Long> {

    // Ensure Zero Duplicates: Find if this account already exists in NRKGo
    Optional<ExternalAccount> findByProviderAndProviderAccountId(String provider, String providerAccountId);

    Optional<ExternalAccount> findByProviderAndEmail(String provider, String email);
}

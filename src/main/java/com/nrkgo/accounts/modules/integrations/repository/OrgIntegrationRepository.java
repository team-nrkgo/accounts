package com.nrkgo.accounts.modules.integrations.repository;

import com.nrkgo.accounts.modules.integrations.model.OrgIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrgIntegrationRepository extends JpaRepository<OrgIntegration, Long> {

    // Contextual Lookup: What account is Org A using for Product X?
    List<OrgIntegration> findByOrgIdAndProductCodeAndStatus(Long orgId, Integer productCode, Integer status);

    Optional<OrgIntegration> findByOrgIdAndProductCodeAndExternalAccount(Long orgId, Integer productCode,
            com.nrkgo.accounts.modules.integrations.model.ExternalAccount externalAccount);
}

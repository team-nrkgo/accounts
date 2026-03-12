package com.nrkgo.accounts.modules.echo.repository;

import com.nrkgo.accounts.modules.echo.model.EchoSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EchoSettingsRepository extends JpaRepository<EchoSettings, Long> {
    Optional<EchoSettings> findByOrgId(Long orgId);
}

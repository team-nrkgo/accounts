package com.nrkgo.accounts.modules.echo.repository;

import com.nrkgo.accounts.modules.echo.model.EchoCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EchoCategoryRepository extends JpaRepository<EchoCategory, Long> {
    List<EchoCategory> findByOrgId(Long orgId);

    Optional<EchoCategory> findByOrgIdAndSlug(Long orgId, String slug);
}

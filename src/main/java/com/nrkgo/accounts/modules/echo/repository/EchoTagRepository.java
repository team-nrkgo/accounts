package com.nrkgo.accounts.modules.echo.repository;

import com.nrkgo.accounts.modules.echo.model.EchoTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EchoTagRepository extends JpaRepository<EchoTag, Long> {
    List<EchoTag> findByOrgId(Long orgId);

    Optional<EchoTag> findByOrgIdAndSlug(Long orgId, String slug);

    List<EchoTag> findByOrgIdAndNameIn(Long orgId, List<String> names);
}

package com.nrkgo.accounts.modules.echo.repository;

import com.nrkgo.accounts.modules.echo.model.EchoSearchResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EchoSearchResultRepository extends JpaRepository<EchoSearchResult, Long> {
    Page<EchoSearchResult> findByOrgId(Long orgId, Pageable pageable);

    List<EchoSearchResult> findByOrgIdAndKeyword(Long orgId, String keyword);
}

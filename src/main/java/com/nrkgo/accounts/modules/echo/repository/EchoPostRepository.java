package com.nrkgo.accounts.modules.echo.repository;

import com.nrkgo.accounts.modules.echo.model.EchoPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface EchoPostRepository extends JpaRepository<EchoPost, Long> {

    Page<EchoPost> findByUserIdAndOrgId(Long userId, Long orgId, Pageable pageable);

    Page<EchoPost> findByUserIdAndOrgIdAndStatus(Long userId, Long orgId, String status, Pageable pageable);

    Page<EchoPost> findByUserIdAndOrgIdAndTitleContainingIgnoreCase(Long userId, Long orgId, String title,
            Pageable pageable);

    Optional<EchoPost> findByIdAndUserIdAndOrgId(Long id, Long userId, Long orgId);

    Page<EchoPost> findByOrgIdAndStatus(Long orgId, String status, Pageable pageable);

    Optional<EchoPost> findByOrgIdAndSlugAndStatus(Long orgId, String slug, String status);

    boolean existsByOrgIdAndSlug(Long orgId, String slug);

    boolean existsByOrgIdAndSlugAndIdNot(Long orgId, String slug, Long id);

    @Query("SELECT p.slug FROM EchoPost p WHERE p.orgId = :orgId AND (p.slug = :baseSlug OR p.slug LIKE CONCAT(:baseSlug, '-%'))")
    List<String> findSlugsByOrgIdAndBaseSlug(@Param("orgId") Long orgId, @Param("baseSlug") String baseSlug);

    Optional<EchoPost> findByExternalIdAndStatus(String externalId, String status);

    Optional<EchoPost> findByExternalId(String externalId);
}

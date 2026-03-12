package com.nrkgo.accounts.modules.snapsteps.repository;

import com.nrkgo.accounts.modules.snapsteps.model.SnapGuide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SnapGuideRepository extends JpaRepository<SnapGuide, Long> {
        List<SnapGuide> findByUserIdAndOrgId(Long userId, Long orgId);

        List<SnapGuide> findByUserIdAndOrgIdAndIsStarred(Long userId, Long orgId, Integer isStarred);

        List<SnapGuide> findByUserIdAndOrgIdAndTitleContainingIgnoreCase(Long userId, Long orgId, String title);

        org.springframework.data.domain.Page<SnapGuide> findByUserIdAndOrgId(Long userId, Long orgId,
                        org.springframework.data.domain.Pageable pageable);

        org.springframework.data.domain.Page<SnapGuide> findByUserIdAndOrgIdAndTitleContainingIgnoreCase(Long userId,
                        Long orgId, String title,
                        org.springframework.data.domain.Pageable pageable);

        Optional<SnapGuide> findByExternalId(String externalId);

        org.springframework.data.domain.Page<SnapGuide> findByUserIdAndOrgIdAndIsStarred(Long userId, Long orgId,
                        Integer isStarred, org.springframework.data.domain.Pageable pageable);
}

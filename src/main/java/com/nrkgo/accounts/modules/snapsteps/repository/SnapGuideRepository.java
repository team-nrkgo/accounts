package com.nrkgo.accounts.modules.snapsteps.repository;

import com.nrkgo.accounts.modules.snapsteps.model.SnapGuide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SnapGuideRepository extends JpaRepository<SnapGuide, Long> {
    List<SnapGuide> findByUserId(Long userId);

    Optional<SnapGuide> findByExternalId(String externalId);
}

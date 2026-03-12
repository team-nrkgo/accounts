package com.nrkgo.accounts.modules.scheduler.repository;

import com.nrkgo.accounts.modules.scheduler.model.JobArguments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobArgumentsRepository extends JpaRepository<JobArguments, Long> {
    Optional<JobArguments> findByOrgIdAndProductCodeAndJobClassAndEntityId(Long orgId, Integer productCode,
            String jobClass, Long entityId);
}

package com.nrkgo.accounts.modules.scheduler.repository;

import com.nrkgo.accounts.modules.scheduler.model.JobRepeatRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobRepeatRuleRepository extends JpaRepository<JobRepeatRule, Long> {
    Optional<JobRepeatRule> findByJobArgsId(Long jobArgsId);
}

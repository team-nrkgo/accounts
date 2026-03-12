package com.nrkgo.accounts.modules.scheduler.repository;

import com.nrkgo.accounts.modules.scheduler.model.JobTrigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobTriggerRepository extends JpaRepository<JobTrigger, Long> {

        @Query(value = "SELECT * FROM job_triggers WHERE status = :status AND start_time <= :time LIMIT :limit", nativeQuery = true)
        List<JobTrigger> findPendingTriggers(@Param("status") int status, @Param("time") long time,
                        @Param("limit") int limit);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("UPDATE JobTrigger j SET j.status = :newStatus, j.modifiedTime = :modifiedTime, j.modifiedBy = :modifiedBy WHERE j.id IN :ids AND j.status = :oldStatus")
        int updateStatusForIds(@Param("ids") List<Long> ids, @Param("oldStatus") int oldStatus,
                        @Param("newStatus") int newStatus, @Param("modifiedTime") long modifiedTime,
                        @Param("modifiedBy") Long modifiedBy);

        void deleteByJobArgsIdAndStatus(Long jobArgsId, int status);

        @Modifying
        @Query("DELETE FROM JobTrigger j WHERE j.status IN (2, 4) AND j.modifiedTime < :expiryTime")
        int deleteOldFinishedJobs(@Param("expiryTime") long expiryTime);
}

package com.nrkgo.accounts.modules.scheduler.repository;

import com.nrkgo.accounts.modules.scheduler.model.ScheduledTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {

    List<ScheduledTask> findByStatusAndExecutionTimeLessThanEqual(
            ScheduledTask.TaskStatus status, Long time);

    List<ScheduledTask> findByTaskTypeAndEntityIdAndStatus(
            String taskType, Long entityId, ScheduledTask.TaskStatus status);
}

package com.nrkgo.accounts.modules.scheduler.service;

import com.nrkgo.accounts.modules.scheduler.model.ScheduledTask;

public interface TaskHandler {
    /**
     * Unique identifier for the type of task (e.g., "ECHO_PUBLISH")
     */
    String getTaskType();

    /**
     * Logic to execute the task
     */
    void execute(ScheduledTask task) throws Exception;
}

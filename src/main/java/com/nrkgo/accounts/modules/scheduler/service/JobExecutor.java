package com.nrkgo.accounts.modules.scheduler.service;

import com.nrkgo.accounts.modules.scheduler.model.JobArguments;
import com.nrkgo.accounts.modules.scheduler.model.JobTrigger;

public interface JobExecutor {
    /**
     * Unique identifier for the job class (e.g., "EchoPublishJob")
     */
    String getJobClass();

    /**
     * Logic to execute the task
     */
    void execute(JobTrigger trigger, JobArguments args) throws Exception;
}

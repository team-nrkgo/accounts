package com.nrkgo.accounts.modules.echo.service;

import com.nrkgo.accounts.modules.echo.dto.EchoPostDto;
import com.nrkgo.accounts.modules.echo.integration.EchoIntegrationService;
import com.nrkgo.accounts.modules.scheduler.model.JobArguments;
import com.nrkgo.accounts.modules.scheduler.model.JobTrigger;
import com.nrkgo.accounts.modules.scheduler.service.JobExecutor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class EchoPublishJob implements JobExecutor {

    private final EchoPostService postService;
    private final EchoIntegrationService integrationService;

    public EchoPublishJob(@Lazy EchoPostService postService, EchoIntegrationService integrationService) {
        this.postService = postService;
        this.integrationService = integrationService;
    }

    @Override
    public String getJobClass() {
        return "EchoPublishJob";
    }

    @Override
    public void execute(JobTrigger trigger, JobArguments args) throws Exception {
        // Use the entityId stored in the Parent (JobArguments)
        Long postId = args.getEntityId();

        // This internal method updates status to 'published' and notifies integrations
        postService.publishPostInternal(postId);
    }
}

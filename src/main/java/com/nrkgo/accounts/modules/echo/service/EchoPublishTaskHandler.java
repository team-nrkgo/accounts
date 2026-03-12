package com.nrkgo.accounts.modules.echo.service;

import com.nrkgo.accounts.modules.echo.dto.EchoPostDto;
import com.nrkgo.accounts.modules.echo.integration.EchoIntegrationService;
import com.nrkgo.accounts.modules.scheduler.model.ScheduledTask;
import com.nrkgo.accounts.modules.scheduler.service.TaskHandler;
import org.springframework.stereotype.Service;

@Service
public class EchoPublishTaskHandler implements TaskHandler {

    private final EchoPostService postService;
    private final EchoIntegrationService integrationService;

    public EchoPublishTaskHandler(EchoPostService postService, EchoIntegrationService integrationService) {
        this.postService = postService;
        this.integrationService = integrationService;
    }

    @Override
    public String getTaskType() {
        return "ECHO_PUBLISH";
    }

    @Override
    public void execute(ScheduledTask task) throws Exception {
        Long postId = task.getEntityId();

        // 1. Fetch the post manually by orgId since it's a scheduled job
        // (The service usually requires a User object, so we might need a workaround or
        // a simpler fetch)
        EchoPostDto post = postService.getPostByIdInternal(postId);

        if (post != null) {
            // 2. Trigger integration publishing
            integrationService.handlePostPublished(post);
        }
    }
}

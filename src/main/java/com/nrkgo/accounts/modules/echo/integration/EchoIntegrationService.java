package com.nrkgo.accounts.modules.echo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nrkgo.accounts.modules.echo.dto.EchoPostDto;
import com.nrkgo.accounts.modules.integrations.model.OrgIntegration;
import com.nrkgo.accounts.modules.integrations.repository.OrgIntegrationRepository;
import com.nrkgo.accounts.modules.integrations.service.IntegrationServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EchoIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(EchoIntegrationService.class);
    private static final Integer ECHO_PRODUCT_CODE = 2;

    private final IntegrationServiceImpl integrationService;
    private final OrgIntegrationRepository orgIntegrationRepository;
    private final WordPressService wordPressService;
    private final ObjectMapper objectMapper;

    public EchoIntegrationService(IntegrationServiceImpl integrationService,
            OrgIntegrationRepository orgIntegrationRepository,
            WordPressService wordPressService,
            ObjectMapper objectMapper) {
        this.integrationService = integrationService;
        this.orgIntegrationRepository = orgIntegrationRepository;
        this.wordPressService = wordPressService;
        this.objectMapper = objectMapper;
    }

    public void handlePostPublished(EchoPostDto postDto) {
        Long orgId = postDto.getOrgId();

        // Find all active integrations for Echo in this Org
        List<OrgIntegration> integrations = orgIntegrationRepository.findByOrgIdAndProductCodeAndStatus(orgId,
                ECHO_PRODUCT_CODE, 1);

        for (OrgIntegration integration : integrations) {
            String provider = integration.getExternalAccount().getProvider();

            if ("wordpress".equalsIgnoreCase(provider)) {
                try {
                    Map<String, String> config = objectMapper.readValue(integration.getConfigJson(), Map.class);
                    String siteUrl = config.get("siteUrl");
                    String username = config.get("username");

                    if (siteUrl != null && username != null) {
                        String appPassword = integrationService.getActiveIntegrationCredential(orgId, ECHO_PRODUCT_CODE,
                                "wordpress");
                        String wpPostId = wordPressService.publishPost(siteUrl, username, appPassword, postDto);

                        if (wpPostId != null) {
                            logger.info("Successfully pushed post {} to WordPress site {}. WP ID: {}", postDto.getId(),
                                    siteUrl, wpPostId);
                            // TODO: Save wpPostId back to EchoPost metadata if needed
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to push post to WordPress for org {}", orgId, e);
                }
            }
        }
    }
}

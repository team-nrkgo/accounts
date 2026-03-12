package com.nrkgo.accounts.modules.integrations.service;

import com.nrkgo.accounts.modules.integrations.model.*;
import com.nrkgo.accounts.modules.integrations.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class IntegrationServiceImpl {

        private final ExternalAccountRepository accountRepository;
        private final UserExternalCredsRepository credsRepository;
        private final OrgIntegrationRepository orgIntegrationRepository;

        public IntegrationServiceImpl(ExternalAccountRepository accountRepository,
                        UserExternalCredsRepository credsRepository,
                        OrgIntegrationRepository orgIntegrationRepository) {
                this.accountRepository = accountRepository;
                this.credsRepository = credsRepository;
                this.orgIntegrationRepository = orgIntegrationRepository;
        }

        /**
         * The Master 'Connect' Flow
         * Handles New Connections, Identity Matching, and Token Updates
         */
        @Transactional
        public ExternalAccount connectSocialAccount(Long userId, String provider, String providerAccountId,
                        String email, String displayName, String avatarUrl,
                        String accessToken, String refreshToken, Long expiryTime,
                        String scopes, AuthType authType) {

                // 1. IDENTITY MASTER: Find or Create the External Identity (Zero Duplicate
                // Rule)
                ExternalAccount account = accountRepository
                                .findByProviderAndProviderAccountId(provider, providerAccountId)
                                .orElse(new ExternalAccount());

                account.setProvider(provider);
                account.setProviderAccountId(providerAccountId);
                account.setEmail(email);
                account.setDisplayName(displayName);
                account.setAvatarUrl(avatarUrl);
                account = accountRepository.save(account);

                // 2. THE VAULT: Link this User to the Identity + Secure the Secret Tokens
                UserExternalCreds creds = credsRepository
                                .findByUserIdAndExternalAccount(userId, account)
                                .orElse(new UserExternalCreds());

                creds.setUserId(userId);
                creds.setExternalAccount(account);
                creds.setAuthType(authType);
                creds.setTokenMain(accessToken); // TODO: Add Encryption Wrapper later
                creds.setTokenSecret(refreshToken);
                creds.setExpiryTime(expiryTime);
                creds.setGrantedScopes(scopes);
                credsRepository.save(creds);

                return account;
        }

        /**
         * The Product Usage Flow
         * Links a connection to a specific Organization for a Product (like Echo)
         */
        @Transactional
        public void activateOrgIntegration(Long orgId, Integer productCode, Long extAccountId, String configJson) {
                ExternalAccount account = accountRepository.findById(extAccountId)
                                .orElseThrow(() -> new IllegalArgumentException("External account not found"));

                OrgIntegration integration = orgIntegrationRepository
                                .findByOrgIdAndProductCodeAndExternalAccount(orgId, productCode, account)
                                .orElse(new OrgIntegration());

                integration.setOrgId(orgId);
                integration.setProductCode(productCode);
                integration.setExternalAccount(account);
                integration.setConfigJson(configJson);
                integration.setStatus(1); // Active
                orgIntegrationRepository.save(integration);
        }

        /**
         * Disconnects an integration from a specific Organization and Product.
         * Sets status to 0 instead of deleting to keep history if needed.
         */
        @Transactional
        public void deactivateOrgIntegration(Long orgId, Integer productCode, Long extAccountId) {
                ExternalAccount account = accountRepository.findById(extAccountId)
                                .orElseThrow(() -> new IllegalArgumentException("External account not found"));

                Optional<OrgIntegration> integrationOpt = orgIntegrationRepository
                                .findByOrgIdAndProductCodeAndExternalAccount(orgId, productCode, account);

                if (integrationOpt.isPresent()) {
                        OrgIntegration integration = integrationOpt.get();
                        integration.setStatus(0); // Inactive/Disconnected
                        orgIntegrationRepository.save(integration);
                }
        }

        /**
         * Completely removes the connection from the user's vault.
         */
        @Transactional
        public void disconnectSocialAccount(Long userId, Long extAccountId) {
                ExternalAccount account = accountRepository.findById(extAccountId)
                                .orElseThrow(() -> new IllegalArgumentException("External account not found"));

                Optional<UserExternalCreds> credsOpt = credsRepository.findByUserIdAndExternalAccount(userId, account);

                if (credsOpt.isPresent()) {
                        credsRepository.delete(credsOpt.get());
                }
        }

        /**
         * Safely retrieves the credentials (e.g. API Key or Access Token) for a
         * specific product and provider.
         * Throws an explicit exception if the user has not connected it, or if it is
         * currently inactive.
         */
        @Transactional(readOnly = true)
        public String getActiveIntegrationCredential(Long orgId, Integer productCode, String provider) {
                // Find all *ACTIVE* integrations for this org/product combination
                java.util.List<OrgIntegration> activeIntegrations = orgIntegrationRepository
                                .findByOrgIdAndProductCodeAndStatus(orgId, productCode, 1);

                // Find the specific provider (e.g., 'gemini')
                OrgIntegration matchingIntegration = activeIntegrations.stream()
                                .filter(i -> i.getExternalAccount().getProvider().equalsIgnoreCase(provider))
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException(
                                                "Please connect your " + provider + " integration first."));

                // Fetch the secure tokens from the vault using the external account ID
                ExternalAccount account = matchingIntegration.getExternalAccount();

                // Fetch the valid credentials for the original connecting user.
                // NOTE: In an org context, the credentials from user_external_creds represent
                // the vault lock.
                java.util.List<UserExternalCreds> credsList = credsRepository.findAll()
                                .stream().filter(c -> c.getExternalAccount().getId().equals(account.getId())).toList();

                if (credsList.isEmpty()) {
                        throw new IllegalStateException("Authentication token not found for " + provider);
                }

                // Return the main token (API Key or Access Token)
                return credsList.get(0).getTokenMain();
        }
}

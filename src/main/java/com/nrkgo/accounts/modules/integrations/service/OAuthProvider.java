package com.nrkgo.accounts.modules.integrations.service;

import com.nrkgo.accounts.modules.integrations.model.ExternalAccount;

public interface OAuthProvider {
    String getProviderName();

    String getAuthorizeUrl(Long userId, Long orgId, Integer productCode, String redirectUri);

    void handleCallback(String code, String state, String redirectUri);
}

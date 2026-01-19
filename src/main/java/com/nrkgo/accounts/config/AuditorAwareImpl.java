package com.nrkgo.accounts.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        // Assuming Principal is likely a UserDetails object or ID. 
        // Logic to extract ID depends on your SecurityUser implementation.
        // For now, returning empty to avoid cast exceptions until Security is fully set up.
        // TODO: Implement ID extraction from CustomUserDetails
        return Optional.empty(); 
    }
}

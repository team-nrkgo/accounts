package com.nrkgo.accounts.modules.plans.admin;

import com.nrkgo.accounts.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates that the caller is a product super-admin (owner-level access).
 *
 * Admin emails are configured in application.properties:
 * app.admin.emails=you@gmail.com,partner@gmail.com
 *
 * To add/remove admins: update the property and restart the server.
 * In production: set the ADMIN_EMAILS environment variable on the server.
 *
 * This is NOT org-level admin. This is the product owner check.
 * It has zero database queries — just a simple email set lookup.
 */
@Component
public class AdminGuard {

    private final Set<String> adminEmails;

    public AdminGuard(@Value("${app.admin.emails:}") String adminEmailsConfig) {
        this.adminEmails = Arrays.stream(adminEmailsConfig.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(e -> !e.isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * Returns true if the user's email is in the app.admin.emails list.
     */
    public boolean isAdmin(User user) {
        if (user == null || user.getEmail() == null)
            return false;
        return adminEmails.contains(user.getEmail().toLowerCase().trim());
    }

    /**
     * Throws SecurityException if the user is not a super-admin.
     * Call this at the top of every /admin/ endpoint.
     */
    public void requireAdmin(User user) {
        if (!isAdmin(user)) {
            throw new SecurityException("Super admin access required");
        }
    }
}

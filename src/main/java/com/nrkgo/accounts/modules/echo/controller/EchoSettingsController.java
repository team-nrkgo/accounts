package com.nrkgo.accounts.modules.echo.controller;

import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.modules.echo.model.EchoSettings;
import com.nrkgo.accounts.modules.echo.repository.EchoSettingsRepository;
import com.nrkgo.accounts.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/echo/settings")
public class EchoSettingsController {

    private final EchoSettingsRepository settingsRepository;
    private final UserService userService;

    public EchoSettingsController(EchoSettingsRepository settingsRepository, UserService userService) {
        this.settingsRepository = settingsRepository;
        this.userService = userService;
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        if (request.getCookies() == null)
            return null;
        for (Cookie cookie : request.getCookies()) {
            if ("user_session".equals(cookie.getName())) {
                return userService.getUserBySession(cookie.getValue());
            }
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<EchoSettings>> getSettings(
            HttpServletRequest request,
            @RequestParam(name = "org_id") Long orgId) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        EchoSettings settings = settingsRepository.findByOrgId(orgId)
                .orElseGet(() -> {
                    EchoSettings newSettings = new EchoSettings();
                    newSettings.setOrgId(orgId);
                    return settingsRepository.save(newSettings);
                });

        return ResponseEntity.ok(ApiResponse.success("Settings fetched", settings));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EchoSettings>> updateSettings(
            HttpServletRequest request,
            @RequestParam(name = "org_id") Long orgId,
            @RequestBody EchoSettings updatedSettings) {

        User user = getAuthenticatedUser(request);
        if (user == null)
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        EchoSettings settings = settingsRepository.findByOrgId(orgId)
                .orElse(new EchoSettings());

        settings.setOrgId(orgId);

        if (updatedSettings.getSiteTitle() != null)
            settings.setSiteTitle(updatedSettings.getSiteTitle());
        if (updatedSettings.getSiteTagline() != null)
            settings.setSiteTagline(updatedSettings.getSiteTagline());
        if (updatedSettings.getTimezone() != null)
            settings.setTimezone(updatedSettings.getTimezone());
        if (updatedSettings.getSiteLanguage() != null)
            settings.setSiteLanguage(updatedSettings.getSiteLanguage());
        if (updatedSettings.getSeoTitle() != null)
            settings.setSeoTitle(updatedSettings.getSeoTitle());
        if (updatedSettings.getSeoDescription() != null)
            settings.setSeoDescription(updatedSettings.getSeoDescription());
        if (updatedSettings.getOgImageUrl() != null)
            settings.setOgImageUrl(updatedSettings.getOgImageUrl());
        if (updatedSettings.getTwitterHandle() != null)
            settings.setTwitterHandle(updatedSettings.getTwitterHandle());
        if (updatedSettings.getMetaJson() != null)
            settings.setMetaJson(updatedSettings.getMetaJson());

        settings = settingsRepository.save(settings);
        return ResponseEntity.ok(ApiResponse.success("Settings updated", settings));
    }
}

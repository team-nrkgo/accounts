package com.nrkgo.accounts.modules.echo.controller;

import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.modules.echo.dto.EchoResearchCaptureDto;
import com.nrkgo.accounts.modules.echo.dto.EchoResearchDetailDto;
import com.nrkgo.accounts.modules.echo.model.EchoSearchResult;
import com.nrkgo.accounts.modules.echo.service.EchoResearchService;
import com.nrkgo.accounts.repository.OrgUserRepository;
import com.nrkgo.accounts.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/echo/research")
public class EchoResearchController {

    private final EchoResearchService service;
    private final UserService userService;
    private final OrgUserRepository orgUserRepository;

    public EchoResearchController(EchoResearchService service, UserService userService,
            OrgUserRepository orgUserRepository) {
        this.service = service;
        this.userService = userService;
        this.orgUserRepository = orgUserRepository;
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("user_session".equals(cookie.getName())) {
                    return userService.getUserBySession(cookie.getValue());
                }
            }
        }
        return null;
    }

    private void validateOrganizationMembership(User user, Long orgId) {
        if (orgId == null) {
            throw new IllegalArgumentException("Organization ID is required");
        }
        boolean isMember = orgUserRepository.existsByOrgIdAndUserId(orgId, user.getId());
        if (!isMember) {
            throw new SecurityException("Access Denied: You are not a member of this organization");
        }
    }

    @PostMapping("/capture")
    public ResponseEntity<ApiResponse<EchoSearchResult>> captureResearch(
            HttpServletRequest request,
            @RequestBody EchoResearchCaptureDto dto) {

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        try {
            validateOrganizationMembership(user, dto.getOrgId());
            EchoSearchResult result = service.captureResearch(dto, user);
            return ResponseEntity.ok(ApiResponse.success("Research content captured successfully", result));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/searches")
    public ResponseEntity<ApiResponse<java.util.List<EchoSearchResult>>> listSearches(
            HttpServletRequest request,
            @RequestParam(name = "org_id") Long orgId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "15") Integer size) {

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        try {
            validateOrganizationMembership(user, orgId);
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdTime").descending());
            Page<EchoSearchResult> results = service.listSearchResults(orgId, pageable);
            return ResponseEntity.ok(ApiResponse.paginatedSuccess("Search history fetched", results));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/searches/{id}")
    public ResponseEntity<ApiResponse<EchoResearchDetailDto>> getSearchDetails(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam(name = "org_id") Long orgId) {

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        try {
            validateOrganizationMembership(user, orgId);
            EchoResearchDetailDto result = service.getSearchResultDetails(id, orgId);
            return ResponseEntity.ok(ApiResponse.success("Search details fetched", result));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }
}

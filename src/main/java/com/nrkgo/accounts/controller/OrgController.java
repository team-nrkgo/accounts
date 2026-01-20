package com.nrkgo.accounts.controller;

import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.dto.InviteUserRequest;
import com.nrkgo.accounts.model.Digest;
import com.nrkgo.accounts.model.Organization;
import com.nrkgo.accounts.service.OrgService;
import com.nrkgo.accounts.service.UserService;
import com.nrkgo.accounts.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orgs")
public class OrgController {

    private final OrgService orgService;
    private final UserService userService;

    // Manual Constructor for Dependency Injection
    public OrgController(OrgService orgService, UserService userService) {
        this.orgService = orgService;
        this.userService = userService;
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        String token = null;
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("user_session".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token == null) return null;
        return userService.getUserBySession(token);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Organization>> createOrganization(
            @Valid @RequestBody com.nrkgo.accounts.dto.CreateOrgRequest request,
            HttpServletRequest httpRequest) {
        
        User user = getAuthenticatedUser(httpRequest);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }
        
        Organization org = orgService.createOrganization(request, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Organization created", org));
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<Organization>> updateOrganization(
            @Valid @RequestBody com.nrkgo.accounts.dto.CreateOrgRequest request,
            HttpServletRequest httpRequest) {
        
        User user = getAuthenticatedUser(httpRequest);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }
        
        Organization org = orgService.updateOrganization(request, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Organization updated", org));
    }

    @PostMapping("/invite")
    public ResponseEntity<ApiResponse<Digest>> inviteUser(
            @Valid @RequestBody InviteUserRequest request,
            HttpServletRequest httpRequest) {
        
        User user = getAuthenticatedUser(httpRequest);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }
        
        Digest invitation = orgService.inviteUser(request, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Invitation sent", invitation));
    }

    @PostMapping("/accept-invite")
    public ResponseEntity<ApiResponse<Void>> acceptInvite(
            @RequestParam String token,
            HttpServletRequest httpRequest) {
        
        // Note: For accept invite, user might already be logged in OR strictly just claiming via token.
        // Assuming current logic just needs a Valid User ID to link. 
        // If the user must be logged in to accept:
        User user = getAuthenticatedUser(httpRequest);
        if (user == null) {
             return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated: Please login to accept invite"));
        }
        
        orgService.acceptInvite(token, user.getId());
        orgService.acceptInvite(token, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Invitation accepted", null));
    }

    @GetMapping("/members")
    public ResponseEntity<ApiResponse<java.util.List<com.nrkgo.accounts.dto.OrgMemberResponse>>> getOrgMembers(
            @RequestParam Long org_id,
            @RequestParam(required = false) String search,
            HttpServletRequest httpRequest) {
        
        User user = getAuthenticatedUser(httpRequest);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }
        
        java.util.List<com.nrkgo.accounts.dto.OrgMemberResponse> members = orgService.getOrgMembers(org_id, user.getId(), search);
        return ResponseEntity.ok(ApiResponse.success("Members fetched", members));
    }
}

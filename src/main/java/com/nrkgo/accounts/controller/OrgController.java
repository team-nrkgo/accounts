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

    @PutMapping
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
        return ResponseEntity.ok(ApiResponse.success("Invitation accepted", null));
    }

    @PostMapping("/claim-invite")
    public ResponseEntity<ApiResponse<Void>> claimInvite(
            @RequestParam Long org_id,
            HttpServletRequest httpRequest) {
        
        User user = getAuthenticatedUser(httpRequest);
        if (user == null) {
             return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated: Please login to accept invite"));
        }
        
        orgService.claimOrgAccess(org_id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Invitation accepted successfully", null));
    }

    @GetMapping("/invitation-details")
    public ResponseEntity<ApiResponse<com.nrkgo.accounts.dto.InvitationDetailsResponse>> getInvitationDetails(
            @RequestParam String token,
            jakarta.servlet.http.HttpServletRequest httpRequest,
            jakarta.servlet.http.HttpServletResponse response) {
        
        // Fetch details AND create session/cookie if valid
        // We will modify the service to return a Pair or DTO with session, 
        // OR we just handle it here if service is purely read-only? 
        // Service needs to create session. 
        // Let's call a new method or overload getInvitationDetails to handle session creation.
        
        // Actually, user wants it "same like login flow".
        // Use orgService to get details + session.
        
        com.nrkgo.accounts.dto.InvitationDetailsResponse details = orgService.getInvitationDetails(token);
        
        // If we are here, token is valid.
        // User wants cookie set NOW.
        // But getInvitationDetails currently just returns info. 
        // I need to fetch the User object associated with this token to create a session.
        
        // Refactoring: service.validateInviteAndLogin(token, request) -> returns Session + Details?
        // Let's keep it simple: calling a new method to get session, then returning details.
        
        com.nrkgo.accounts.model.UserSession session = orgService.createSessionFromInvite(token, httpRequest);
        
        if (session != null) {
            jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("user_session", session.getCookie());
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // Localhost
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);
        }

        return ResponseEntity.ok(ApiResponse.success("Invitation details fetched", details));
    }

    @PostMapping("/claim-account")
    public ResponseEntity<ApiResponse<com.nrkgo.accounts.model.UserSession>> claimAccount(
            @Valid @RequestBody com.nrkgo.accounts.dto.ClaimAccountRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest,
            jakarta.servlet.http.HttpServletResponse response) {
        
        com.nrkgo.accounts.model.UserSession session = orgService.claimAccount(
                request.getToken(), request.getPassword(), request.getFirstName(), request.getLastName(), httpRequest);
        
        // Auto-Login: Set Cookie
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("user_session", session.getCookie());
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);

        return ResponseEntity.ok(ApiResponse.success("Account activated successfully", session));
    }

    @GetMapping("/invite-token")
    public ResponseEntity<ApiResponse<String>> getInviteToken(
            @RequestParam Long memberId,
            HttpServletRequest httpRequest) {
        
        User user = getAuthenticatedUser(httpRequest);
        if (user == null) {
             return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }
        
        // Note: memberId is the OrgUser ID (membership ID), not the User ID
        try {
            String token = orgService.getOrGenerateInviteToken(user.getId(), memberId);
            return ResponseEntity.ok(ApiResponse.success("Token retrieved", token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
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

    @PutMapping("/member")
    public ResponseEntity<ApiResponse<Void>> updateMember(
            @Valid @RequestBody com.nrkgo.accounts.dto.UpdateMemberRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        orgService.updateMember(request, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Member updated", null));
    }

    @DeleteMapping("/member")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @RequestParam Long org_id,
            @RequestParam Long member_id,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        orgService.removeMember(org_id, member_id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Member removed", null));
    }

    // --- Role Management Endpoints ---

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<java.util.List<com.nrkgo.accounts.model.Role>>> getOrgRoles(
            @RequestParam Long org_id,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);
        if (user == null) return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        return ResponseEntity.ok(ApiResponse.success("Roles fetched", orgService.getOrgRoles(org_id)));
    }

    @PostMapping("/roles")
    public ResponseEntity<ApiResponse<com.nrkgo.accounts.model.Role>> createRole(
            @RequestParam Long org_id,
            @Valid @RequestBody com.nrkgo.accounts.dto.RoleRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);
        if (user == null) return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        return ResponseEntity.ok(ApiResponse.success("Role created", orgService.createOrgRole(request, org_id, user.getId())));
    }

    @PutMapping("/roles/{roleId}")
    public ResponseEntity<ApiResponse<com.nrkgo.accounts.model.Role>> updateRole(
            @PathVariable Long roleId,
            @RequestParam Long org_id,
            @Valid @RequestBody com.nrkgo.accounts.dto.RoleRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);
        if (user == null) return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        return ResponseEntity.ok(ApiResponse.success("Role updated", orgService.updateOrgRole(roleId, request, org_id, user.getId())));
    }

    @DeleteMapping("/roles/{roleId}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @PathVariable Long roleId,
            @RequestParam Long org_id,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);
        if (user == null) return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));

        orgService.removeOrgRole(roleId, org_id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Role deleted", null));
    }
}


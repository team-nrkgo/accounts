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


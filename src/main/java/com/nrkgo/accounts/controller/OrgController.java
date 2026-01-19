package com.nrkgo.accounts.controller;

import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.dto.InviteUserRequest;
import com.nrkgo.accounts.model.Digest;
import com.nrkgo.accounts.service.OrgService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orgs")
public class OrgController {

    private final OrgService orgService;

    // Manual Constructor for Dependency Injection
    public OrgController(OrgService orgService) {
        this.orgService = orgService;
    }

    // TODO: Add proper security context retrieval for userId (using SecurityContextHolder)
    // For now passing userId/orgId explicitly or assuming context.

    @PostMapping("/{orgId}/invite")
    public ResponseEntity<ApiResponse<Digest>> inviteUser(
            @PathVariable Long orgId,
            @Valid @RequestBody InviteUserRequest request) {
        
        Digest invitation = orgService.inviteUser(orgId, request);
        return ResponseEntity.ok(ApiResponse.success("Invitation sent", invitation));
    }

    @PostMapping("/accept-invite")
    public ResponseEntity<ApiResponse<Void>> acceptInvite(
            @RequestParam String token,
            @RequestParam Long userId) { // In real app, userId comes from authenticated session
        
        orgService.acceptInvite(token, userId);
        return ResponseEntity.ok(ApiResponse.success("Invitation accepted", null));
    }
}

package com.nrkgo.accounts.modules.drive.controller;

import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.modules.drive.model.DriveFile;
import com.nrkgo.accounts.modules.drive.service.DriveStorageService;
import com.nrkgo.accounts.repository.OrgUserRepository;
import com.nrkgo.accounts.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/drive/v1")
public class DriveFileController {

    private final DriveStorageService driveStorageService;
    private final UserService userService;
    private final OrgUserRepository orgUserRepository;

    public DriveFileController(DriveStorageService driveStorageService, UserService userService,
            OrgUserRepository orgUserRepository) {
        this.driveStorageService = driveStorageService;
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

    @PostMapping("/files/upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file,
            @RequestParam("org_id") Long orgId,
            @RequestParam("product") String productModule,
            @RequestParam(value = "access_level", defaultValue = "USER_PRIVATE") String accessLevel) {

        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        try {
            boolean isMember = orgUserRepository.existsByOrgIdAndUserId(orgId, user.getId());
            if (!isMember) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Access Denied: You are not a member of this organization"));
            }

            DriveFile savedFile = driveStorageService.uploadFile(file, productModule.toUpperCase(), orgId, user.getId(),
                    accessLevel.toUpperCase());

            Map<String, String> responseData = new HashMap<>();
            responseData.put("external_id", savedFile.getExternalId());
            responseData.put("file_name", savedFile.getFileName());
            responseData.put("url", driveStorageService.generatePublicUrl(savedFile));

            return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", responseData));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to upload file: " + e.getMessage()));
        }
    }
}

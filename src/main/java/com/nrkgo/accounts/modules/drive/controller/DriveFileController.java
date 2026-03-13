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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.InputStreamResource;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import java.io.InputStream;

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
                    accessLevel.toUpperCase(), null);

            Map<String, String> responseData = new HashMap<>();
            responseData.put("external_id", savedFile.getExternalId());
            responseData.put("file_name", savedFile.getFileName());
            responseData.put("url", driveStorageService.generatePublicUrl(savedFile));

            return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", responseData));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to upload file: " + e.getMessage()));
        }
    }

    @GetMapping("/files/download/{externalId}")
    public ResponseEntity<?> downloadFile(HttpServletRequest request, @PathVariable String externalId) {
        try {
            DriveFile file = driveStorageService.getFileByExternalId(externalId);

            // 1. If PUBLIC, serve immediately
            if ("PUBLIC".equals(file.getAccessLevel())) {
                return serveFile(file);
            }

            // 2. Auth Required for ORG_SHARED and USER_PRIVATE
            User user = getAuthenticatedUser(request);
            if (user == null) {
                return ResponseEntity.status(401).build();
            }

            // 3. Check USER_PRIVATE
            if ("USER_PRIVATE".equals(file.getAccessLevel())) {
                if (!file.getUserId().equals(user.getId())) {
                    return ResponseEntity.status(403).build();
                }
            }

            // 4. Check ORG_SHARED
            if ("ORG_SHARED".equals(file.getAccessLevel())) {
                boolean isMember = orgUserRepository.existsByOrgIdAndUserId(file.getOrgId(), user.getId());
                if (!isMember) {
                    return ResponseEntity.status(403).build();
                }
            }

            return serveFile(file);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private ResponseEntity<?> serveFile(DriveFile file) {
        try {
            ResponseInputStream<GetObjectResponse> s3Stream = driveStorageService.getFileStream(file);
            InputStreamResource resource = new InputStreamResource(s3Stream);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                    .contentType(MediaType.parseMediaType(file.getFileType()))
                    .contentLength(file.getFileSize())
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

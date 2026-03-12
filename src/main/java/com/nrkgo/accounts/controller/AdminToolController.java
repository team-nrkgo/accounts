package com.nrkgo.accounts.controller;

import com.nrkgo.accounts.common.response.ApiResponse;
import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.modules.plans.admin.AdminGuard;
import com.nrkgo.accounts.service.AdminToolService;
import com.nrkgo.accounts.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Admin Tool to browse database tables.
 * Only accessible by Product Admins (Owners).
 */
@RestController
@RequestMapping("/api/admin/tables")
public class AdminToolController {

    private final AdminToolService adminToolService;
    private final UserService userService;
    private final AdminGuard adminGuard;

    public AdminToolController(AdminToolService adminToolService, UserService userService, AdminGuard adminGuard) {
        this.adminToolService = adminToolService;
        this.userService = userService;
        this.adminGuard = adminGuard;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<String>>> listTables(HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }
        try {
            adminGuard.requireAdmin(user);
            return ResponseEntity
                    .ok(ApiResponse.success("Available tables fetched", adminToolService.getAvailableTables()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/schema")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTableSchema(
            @RequestParam("table_name") String tableName,
            HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }
        try {
            adminGuard.requireAdmin(user);
            return ResponseEntity.ok(ApiResponse.success("Schema fetched for " + tableName,
                    adminToolService.getTableSchema(tableName)));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTableData(
            @RequestParam("table_name") String tableName,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "DESC") String sortOrder,
            HttpServletRequest request) {
        User user = getAuthenticatedUser(request);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }
        try {
            adminGuard.requireAdmin(user);

            Map<String, Object> response = new HashMap<>();
            response.put("data", adminToolService.getTableData(tableName, limit, offset, sortBy, sortOrder));
            response.put("total", adminToolService.getTableCount(tableName));
            response.put("limit", limit);
            response.put("offset", offset);
            response.put("table_name", tableName);

            return ResponseEntity.ok(ApiResponse.success("Data fetched for " + tableName, response));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
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
        if (token == null)
            return null;
        return userService.getUserBySession(token);
    }
}

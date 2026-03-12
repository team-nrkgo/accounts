package com.nrkgo.accounts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminToolService {

    private final JdbcTemplate jdbcTemplate;

    public AdminToolService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Value("${app.admin.restricted-tables:}")
    private String restrictedTablesConfig;

    public List<String> getRestrictedTables() {
        if (restrictedTablesConfig == null || restrictedTablesConfig.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(restrictedTablesConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public List<String> getAvailableTables() {
        String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE()";
        List<String> allTables = jdbcTemplate.queryForList(sql, String.class);
        List<String> restricted = getRestrictedTables();

        return allTables.stream()
                .filter(t -> !restricted.contains(t))
                .collect(Collectors.toList());
    }

    public boolean isTableAllowed(String tableName) {
        if (tableName == null || tableName.isEmpty())
            return false;
        List<String> restricted = getRestrictedTables();
        return !restricted.contains(tableName);
    }

    public List<Map<String, Object>> getTableSchema(String tableName) {
        if (!isTableAllowed(tableName)) {
            throw new SecurityException("Table access not allowed: " + tableName);
        }

        // For MySQL
        String sql = "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_KEY, COLUMN_TYPE " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? " +
                "ORDER BY ORDINAL_POSITION";

        return jdbcTemplate.queryForList(sql, tableName);
    }

    public List<Map<String, Object>> getTableData(String tableName, int limit, int offset, String sortBy,
            String sortOrder) {
        if (!isTableAllowed(tableName)) {
            throw new SecurityException("Table access not allowed: " + tableName);
        }

        // Basic validation for sortBy to prevent SQL injection
        if (sortBy != null && !sortBy.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid sort column");
        }

        if (sortOrder != null && !sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
            throw new IllegalArgumentException("Invalid sort order");
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM `").append(tableName).append("` ");

        if (sortBy != null && !sortBy.isEmpty()) {
            sql.append(" ORDER BY `").append(sortBy).append("` ").append(sortOrder != null ? sortOrder : "ASC");
        }

        sql.append(" LIMIT ? OFFSET ?");

        return jdbcTemplate.queryForList(sql.toString(), limit, offset);
    }

    public long getTableCount(String tableName) {
        if (!isTableAllowed(tableName)) {
            throw new SecurityException("Table access not allowed: " + tableName);
        }
        String sql = "SELECT COUNT(*) FROM `" + tableName + "`";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}

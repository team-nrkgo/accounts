-- SnapSteps Module Schema

-- 1. Guides Table
CREATE TABLE IF NOT EXISTS ss_guides (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,      -- Clean DB managed ID
    external_id VARCHAR(50) NOT NULL UNIQUE,   -- Extension managed ID (guide_17...)
    user_id BIGINT NOT NULL,
    org_id BIGINT NOT NULL,                    -- Added for multi-tenancy
    title VARCHAR(255) DEFAULT 'Untitled Workflow',
    steps_json LONGTEXT NOT NULL,
    total_steps INT DEFAULT 0,
    first_url TEXT,
    storage_type VARCHAR(20) DEFAULT 'cloud',  -- 'cloud' or 'local'
    created_by BIGINT,
    created_time BIGINT,
    modified_by BIGINT,
    modified_time BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE,
    INDEX (user_id),
    INDEX (org_id),
    INDEX (external_id)
);

-- 2. Usage Tracking Table (Isolated to SnapSteps)
CREATE TABLE IF NOT EXISTS ss_usage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    org_id BIGINT NOT NULL,                    -- Added for multi-tenancy
    guides_count INT DEFAULT 0,                -- Current number of saved guides
    exports_used INT DEFAULT 0,                -- Number of PDF/link exports made this cycle
    reset_time BIGINT,                         -- When usage counters reset (for monthly limits)
    created_time BIGINT,
    modified_time BIGINT,
    UNIQUE KEY idx_user_org (user_id, org_id), -- One usage row per user per org
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE
);

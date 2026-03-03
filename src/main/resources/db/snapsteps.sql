-- SnapSteps Module Schema (Clean ID Version)
CREATE TABLE IF NOT EXISTS ss_guides (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- Clean DB managed ID
    external_id VARCHAR(50) NOT NULL UNIQUE, -- Extension managed ID (guide_17...)
    user_id BIGINT NOT NULL,
    title VARCHAR(255) DEFAULT 'Untitled Workflow',
    steps_json LONGTEXT NOT NULL,    
    total_steps INT DEFAULT 0,
    first_url TEXT,
    storage_type VARCHAR(20) DEFAULT 'cloud',
    created_by BIGINT,
    created_time BIGINT,
    modified_by BIGINT,
    modified_time BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX (user_id),
    INDEX (external_id)
);

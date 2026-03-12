-- NRKGo Integration Hub Schema (v1.0)
-- 10-Year SaaS Expert Pattern: Master Identity -> Encrypted Vault -> Org Usage

-- 1. Master Identity: Ensures Zero Duplication across the platform
CREATE TABLE IF NOT EXISTS external_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider VARCHAR(50) NOT NULL,          -- e.g., 'GOOGLE', 'FACEBOOK', 'WORDPRESS', 'WIX'
    provider_account_id VARCHAR(255) NOT NULL, -- Unique ID from the provider (e.g. Google Sub ID)
    email VARCHAR(255),
    display_name VARCHAR(255),
    avatar_url TEXT,
    created_time BIGINT,
    UNIQUE KEY uq_provider_account (provider, provider_account_id),
    INDEX idx_provider_email (provider, email)
);

-- 2. The Vault: Encrypted Credentials for Users
CREATE TABLE IF NOT EXISTS user_external_creds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    ext_account_id BIGINT NOT NULL,
    auth_type ENUM('OAUTH', 'API_KEY', 'BASIC_AUTH') NOT NULL,
    token_main LONGTEXT NOT NULL,           -- Encrypted: Access Token or API Key
    token_secret LONGTEXT,                  -- Encrypted: Refresh Token or Secret Key
    granted_scopes TEXT,                    -- The scopes currently active for this token
    expiry_time BIGINT,                     -- Null for API Keys
    meta_json JSON,                         -- Extra provider-specific technical info
    created_time BIGINT,
    modified_time BIGINT,
    FOREIGN KEY (ext_account_id) REFERENCES external_accounts(id),
    INDEX idx_user_account (user_id, ext_account_id)
);

-- 3. Contextual Usage: Maps a connection to a specific Product & Organization
CREATE TABLE IF NOT EXISTS org_integrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    org_id BIGINT NOT NULL,
    product_code INT NOT NULL,              -- e.g., 2 for ECHO
    ext_account_id BIGINT NOT NULL,
    config_json JSON,                       -- Config like (target_blog_id, folder_name, etc.)
    status INT DEFAULT 1,                   -- 1: Active, 0: Disabled
    created_time BIGINT,
    FOREIGN KEY (ext_account_id) REFERENCES external_accounts(id),
    INDEX idx_org_product (org_id, product_code)
);

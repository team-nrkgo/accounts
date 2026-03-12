-- Plans & Subscriptions Module Schema
-- Plans belong to ORGANIZATIONS, not individual users.

-- 1. Products Registry (Lightweight — uniqueness enforcement + slug/metadata store)
--    HOW TO REGISTER A NEW PRODUCT:
--    1. INSERT a row here with the next available product_code
--    2. Add plans for it below
--    3. Add the constant in ProductCodes.java
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code INT NOT NULL,          -- Must be unique. Use next sequential number.
    slug VARCHAR(100) NOT NULL,         -- URL path slug: e.g. "snapsteps" → GET /snapsteps/init
    name VARCHAR(200) NOT NULL,
    description TEXT,
    status INT DEFAULT 1,               -- 1: Active, 0: Deprecated
    created_time BIGINT,
    UNIQUE KEY uq_product_code (product_code),
    UNIQUE KEY uq_product_slug (slug)
);

-- 2. Plans Table
CREATE TABLE IF NOT EXISTS plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code INT NOT NULL,
    plan_name VARCHAR(100) NOT NULL,
    plan_type INT NOT NULL,             -- 1: Free, 2: Paid, 3: Lifetime
    price DECIMAL(10,2) DEFAULT 0.00,
    currency VARCHAR(10) DEFAULT 'USD',
    features_json LONGTEXT NOT NULL,
    status INT DEFAULT 1,               -- 1: Active, 0: Deprecated
    created_time BIGINT,
    modified_time BIGINT,
    INDEX (product_code),
    UNIQUE KEY uq_product_plan (product_code, plan_name),
    FOREIGN KEY fk_plan_product (product_code) REFERENCES products(product_code)
);

-- 3. Subscriptions (Org-level plan history)
CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    org_id BIGINT NOT NULL,
    product_code INT NOT NULL,
    plan_id BIGINT NOT NULL,
    previous_plan_id BIGINT,
    locked_features_json LONGTEXT,
    activation_source VARCHAR(50) DEFAULT 'system',
    start_time BIGINT,
    expiry_time BIGINT,
    status INT DEFAULT 1,               -- 1: Active, 0: Expired, 2: Cancelled
    activated_by BIGINT,
    created_time BIGINT,
    modified_time BIGINT,
    FOREIGN KEY (org_id) REFERENCES organizations(id),
    FOREIGN KEY (plan_id) REFERENCES plans(id),
    FOREIGN KEY fk_sub_product (product_code) REFERENCES products(product_code),
    INDEX idx_active_sub (org_id, product_code, status)
);

-- ===========================================================================
-- SEED DATA
-- ===========================================================================

-- SnapSteps (product_code = 1)
INSERT IGNORE INTO products (product_code, slug, name, description, status, created_time) VALUES
(1, 'snapsteps', 'SnapSteps', 'Interactive step-by-step guide builder', 1, UNIX_TIMESTAMP() * 1000);

-- Echo (product_code = 2)
INSERT IGNORE INTO products (product_code, slug, name, description, status, created_time) VALUES
(2, 'echo', 'Echo', 'AI-powered blog publishing platform', 1, UNIX_TIMESTAMP() * 1000);

INSERT IGNORE INTO plans (product_code, plan_name, plan_type, price, currency, features_json, status, created_time) VALUES
-- Starter: Free
(1, 'Starter', 1, 0.00, 'USD',
 '{"max_guides": -1, "auto_screenshot": true, "export_pdf": true, "export_html": true, "export_markdown": true, "personal_org": true, "local_storage": true, "walkthrough_playback": true, "cloud_sync": false, "invite_members": false, "remove_branding": false, "blur_sensitive": false, "custom_brand_color": false, "priority_support": false, "max_members": 1, "shared_library": false, "role_based_access": false, "guide_folders": false, "public_guide_links": false, "team_analytics": false, "sso": false, "api_access": false, "audit_logs": false}',
 1, UNIX_TIMESTAMP() * 1000),

-- Pro: $9/mo
(1, 'Pro', 2, 9.00, 'USD',
 '{"max_guides": -1, "auto_screenshot": true, "export_pdf": true, "export_html": true, "export_markdown": true, "personal_org": true, "local_storage": true, "walkthrough_playback": true, "cloud_sync": true, "invite_members": false, "remove_branding": true, "blur_sensitive": true, "custom_brand_color": true, "priority_support": true, "max_members": 1, "shared_library": false, "role_based_access": false, "guide_folders": false, "public_guide_links": false, "team_analytics": false, "sso": false, "api_access": false, "audit_logs": false}',
 1, UNIX_TIMESTAMP() * 1000),

-- Team: $19/user/mo
(1, 'Team', 2, 19.00, 'USD',
 '{"max_guides": -1, "auto_screenshot": true, "export_pdf": true, "export_html": true, "export_markdown": true, "personal_org": true, "local_storage": true, "walkthrough_playback": true, "cloud_sync": true, "invite_members": true, "remove_branding": true, "blur_sensitive": true, "custom_brand_color": true, "priority_support": true, "max_members": -1, "min_members": 2, "shared_library": true, "role_based_access": true, "guide_folders": true, "public_guide_links": true, "team_analytics": true, "sso": false, "api_access": false, "audit_logs": false}',
 1, UNIX_TIMESTAMP() * 1000),

-- Enterprise: Custom
(1, 'Enterprise', 2, 0.00, 'USD',
 '{"max_guides": -1, "auto_screenshot": true, "export_pdf": true, "export_html": true, "export_markdown": true, "personal_org": true, "local_storage": true, "walkthrough_playback": true, "cloud_sync": true, "invite_members": true, "remove_branding": true, "blur_sensitive": true, "custom_brand_color": true, "priority_support": true, "max_members": -1, "min_members": -1, "shared_library": true, "role_based_access": true, "guide_folders": true, "public_guide_links": true, "team_analytics": true, "sso": true, "scim": true, "api_access": true, "audit_logs": true, "sla": true, "dedicated_success_manager": true}',
 1, UNIX_TIMESTAMP() * 1000),

-- Echo Starter
(2, 'Starter', 1, 0.00, 'USD',
 '{"max_posts": 5, "ai_generation": true, "custom_domain": false, "remove_branding": false, "analytics": false, "api_access": false}',
 1, UNIX_TIMESTAMP() * 1000),

-- Echo Pro
(2, 'Pro', 2, 15.00, 'USD',
 '{"max_posts": -1, "ai_generation": true, "custom_domain": true, "remove_branding": true, "analytics": true, "api_access": false}',
 1, UNIX_TIMESTAMP() * 1000),

-- Echo Team
(2, 'Team', 2, 49.00, 'USD',
 '{"max_posts": -1, "ai_generation": true, "custom_domain": true, "remove_branding": true, "analytics": true, "api_access": true, "team_collaboration": true}',
 1, UNIX_TIMESTAMP() * 1000);

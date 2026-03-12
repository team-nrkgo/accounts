-- Echo Module Schema
-- Based on SnapSteps architecture

CREATE TABLE IF NOT EXISTS echo_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    org_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    created_time BIGINT,
    UNIQUE KEY uq_echo_category_slug (org_id, slug),
    INDEX idx_echo_cat_org (org_id)
);

CREATE TABLE IF NOT EXISTS echo_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    org_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    created_time BIGINT,
    UNIQUE KEY uq_echo_tag_slug (org_id, slug),
    INDEX idx_echo_tag_org (org_id)
);

CREATE TABLE IF NOT EXISTS echo_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    external_id VARCHAR(50) NOT NULL,    -- UUID for public/API access
    user_id BIGINT NOT NULL,             -- Owner
    org_id BIGINT NOT NULL,              -- Organization context
    category_id BIGINT,                  -- Optional category
    title VARCHAR(500) NOT NULL,
    slug VARCHAR(500) NOT NULL,          -- URL friendly path
    featured_image_url TEXT,             -- Sidebar featured image
    content_json LONGTEXT,               -- Tiptap document nodes
    metadata_json TEXT,                  -- SEO metadata, etc.
    status ENUM('draft', 'published', 'scheduled', 'trash') DEFAULT 'draft',
    scheduled_time BIGINT,               -- For 'Post Timing' feature
    created_time BIGINT,
    modified_time BIGINT,
    UNIQUE KEY uq_echo_external_id (external_id),
    INDEX idx_echo_user_org (user_id, org_id),
    INDEX idx_echo_status (status),
    INDEX idx_echo_category (category_id),
    INDEX idx_echo_post_slug (org_id, slug(255))
);

CREATE TABLE IF NOT EXISTS echo_post_tags (
    post_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (post_id, tag_id)
);

CREATE TABLE IF NOT EXISTS echo_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    org_id BIGINT NOT NULL,
    site_title VARCHAR(255),
    site_tagline VARCHAR(255),
    timezone VARCHAR(100) DEFAULT 'UTC',
    site_language VARCHAR(10) DEFAULT 'en',
    seo_title VARCHAR(255),
    seo_description TEXT,
    og_image_url TEXT,
    twitter_handle VARCHAR(100),
    meta_json TEXT, -- For any extra settings
    modified_time BIGINT,
    UNIQUE KEY uq_echo_settings_org (org_id)
);

CREATE TABLE IF NOT EXISTS echo_search_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    org_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    keyword VARCHAR(500) NOT NULL,
    source_url TEXT,
    location VARCHAR(50),
    total_questions INT DEFAULT 0,
    created_time BIGINT,
    INDEX idx_echo_search_org (org_id),
    INDEX idx_echo_search_keyword (keyword(255))
);

CREATE TABLE IF NOT EXISTS echo_paa_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    search_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    answer_url TEXT,
    domain VARCHAR(255),
    position INT,
    extra_data_json TEXT, -- For snippets, metadata, etc.
    FOREIGN KEY (search_id) REFERENCES echo_search_results(id) ON DELETE CASCADE,
    INDEX idx_echo_paa_search (search_id)
);

-- Accounts Service Schema
-- Safe to run multiple times: all CREATE TABLE use IF NOT EXISTS, all INSERTs use INSERT IGNORE
 create database  IF NOT EXISTS accounts;
 use accounts;
-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    mobile_number VARCHAR(50),
    country VARCHAR(10),
    time_zone VARCHAR(50),
    status INT DEFAULT 0,        -- 0: Created, 1: Active
    source INT,                  -- 1: Direct, 2: Google, etc.
    source_id VARCHAR(255),
    mfa_enabled INT DEFAULT 0,
    profile_id BIGINT,
    created_by BIGINT,
    created_time BIGINT,
    modified_by BIGINT,
    modified_time BIGINT
);

-- 2. Roles Table
-- org_id = NULL → system-wide role (Admin, Editor, Viewer)
-- org_id = set  → custom role scoped to that org
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    org_id BIGINT,
    created_by BIGINT,
    created_time BIGINT,
    modified_by BIGINT,
    modified_time BIGINT,
    UNIQUE KEY uq_role_org_name (org_id, name)  -- Unique per org scope (NULL = system)
);

-- Seed system-wide roles
INSERT IGNORE INTO roles (id, name, description) VALUES (1, 'Admin',  'Administrator with full access');
INSERT IGNORE INTO roles (id, name, description) VALUES (2, 'Editor', 'Can edit content but cannot manage users');
INSERT IGNORE INTO roles (id, name, description) VALUES (3, 'Viewer', 'Read-only access');

-- 3. Organizations Table
CREATE TABLE IF NOT EXISTS organizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    org_name VARCHAR(255) NOT NULL,
    org_url_name VARCHAR(255) NOT NULL,  -- unique slug for URLs
    org_type INT,
    website VARCHAR(255),
    employee_count VARCHAR(255),
    mobile VARCHAR(50),
    description TEXT,
    application_name VARCHAR(255),
    app_icon_dark BIGINT,
    app_icon_light BIGINT,
    status INT DEFAULT 1,
    created_by BIGINT,
    created_time BIGINT,
    modified_by BIGINT,
    modified_time BIGINT
);

-- 4. Org Users Table (Many-to-Many: User ↔ Org with Role)
CREATE TABLE IF NOT EXISTS org_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    org_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    status INT DEFAULT 0,       -- 1: Active, 0: Pending/Invited
    is_default INT DEFAULT 0,   -- 1: This is the user's default org
    designation VARCHAR(255),
    created_by BIGINT,
    created_time BIGINT,
    modified_by BIGINT,
    modified_time BIGINT,
    FOREIGN KEY (org_id) REFERENCES organizations(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- 5. Digests Table (Tokens for invites, password resets, verifications)
CREATE TABLE IF NOT EXISTS digests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,   -- e.g., INVITE, RESET_PASSWORD, VERIFY_EMAIL
    entity_id VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    metadata TEXT,
    expiry_time BIGINT NOT NULL,
    created_by BIGINT,
    created_time BIGINT,
    modified_by BIGINT,
    modified_time BIGINT
);

-- 6. User Sessions Table
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    browser VARCHAR(50),
    cookie VARCHAR(255),
    device_name VARCHAR(255),
    location INT,
    status INT NOT NULL,
    device_os VARCHAR(50),
    expire_time BIGINT NOT NULL,
    machine_ip VARCHAR(255),
    city VARCHAR(100),
    created_by BIGINT,
    created_time BIGINT,
    modified_by BIGINT,
    modified_time BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

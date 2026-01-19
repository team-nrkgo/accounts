-- Accounts Service Schema
-- Generated based on JPA Entities

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
    status INT DEFAULT 0, -- 0: Created, 1: Active
    source INT, -- 1: Direct, 2: Google, etc.
    source_id VARCHAR(255),
    mfa_enabled INT DEFAULT 0,
    profile_id BIGINT,
    created_by BIGINT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_by BIGINT,
    modified_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Roles Table
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE, -- e.g., ROLE_ADMIN, ROLE_USER
    description VARCHAR(255),
    created_by BIGINT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_by BIGINT,
    modified_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 3. Organizations Table
CREATE TABLE IF NOT EXISTS organizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    org_name VARCHAR(255) NOT NULL,
    org_url_name VARCHAR(255) NOT NULL, -- unique ID/slug for URLs
    org_type INT, 
    website VARCHAR(255),
    employee_count VARCHAR(255),
    description TEXT,
    application_name VARCHAR(255),
    app_icon_dark BIGINT,
    app_icon_light BIGINT,
    status INT DEFAULT 1, -- 1: Active
    created_by BIGINT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_by BIGINT,
    modified_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 4. Org Users Table (Many-to-Many User <-> Org with Role)
CREATE TABLE IF NOT EXISTS org_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    org_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    status INT DEFAULT 0, -- 1: Active, 0: Pending/Invited
    designation VARCHAR(255),
    created_by BIGINT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_by BIGINT,
    modified_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (org_id) REFERENCES organizations(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- 5. Digests Table (Tokens for invites, resets, etc.)
CREATE TABLE IF NOT EXISTS digests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL, -- e.g., INVITE, RESET_PASSWORD
    entity_id VARCHAR(255) NOT NULL,  -- ID of the entity
    token VARCHAR(255) NOT NULL UNIQUE,
    metadata TEXT,
    expiry_time TIMESTAMP NOT NULL,
    created_by BIGINT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_by BIGINT,
    modified_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 6. User Sessions Table
CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    browser INT,
    cookie VARCHAR(255),
    device_name VARCHAR(255),
    location INT,
    status INT NOT NULL,
    device_os INT,
    expire_time TIMESTAMP NOT NULL,
    machine_ip VARCHAR(255),
    city VARCHAR(100),
    created_by BIGINT,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_by BIGINT,
    modified_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Plans & Subscriptions Module Schema
-- products table REMOVED: product context is handled by product_code column
-- Plan limits are stored in features_json (modern equivalent of .properties files)

-- 1. Plans Table (Definitions — replaces properties files)
CREATE TABLE IF NOT EXISTS plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code INT NOT NULL,              -- 101: SnapSteps, 102: next product...
    plan_name VARCHAR(100) NOT NULL,
    plan_type INT NOT NULL,                 -- 1: Free, 2: Paid, 3: Lifetime
    price DECIMAL(10,2) DEFAULT 0.00,
    currency VARCHAR(10) DEFAULT 'USD',
    -- Stores all limits & feature flags (replaces .properties files)
    -- Example: {"max_guides": 5, "cloud_storage": false, "export_allowed": false, "max_steps_per_guide": 20}
    features_json LONGTEXT NOT NULL,
    status INT DEFAULT 1,                   -- 1: Active, 0: Deprecated
    created_time BIGINT,
    modified_time BIGINT,
    INDEX (product_code)
);

-- 2. Subscriptions (Which plan is this user on?)
CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_code INT NOT NULL,              -- Which product (101, 102...)
    plan_id BIGINT NOT NULL,
    external_id VARCHAR(255),               -- Stripe Sub ID, AppSumo Key, etc.
    start_time BIGINT,
    expiry_time BIGINT,                     -- -1 for lifetime/free
    status INT DEFAULT 1,                   -- 1: Active, 0: Expired, 2: Cancelled
    metadata_json LONGTEXT,                 -- e.g. {"stack_count": 2} for license stacking
    created_time BIGINT,
    modified_time BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (plan_id) REFERENCES plans(id),
    INDEX (user_id),
    INDEX (product_code),
    INDEX (external_id)
);

-- Seed SnapSteps Plans (product_code = 101)
INSERT IGNORE INTO plans (product_code, plan_name, plan_type, price, features_json, status, created_time) VALUES
(101, 'Free',         1, 0.00,  '{"max_guides": 5, "cloud_storage": false, "export_allowed": false, "max_steps_per_guide": 20}', 1, UNIX_TIMESTAMP() * 1000),
(101, 'Pro',          2, 19.00, '{"max_guides": -1, "cloud_storage": true,  "export_allowed": true,  "max_steps_per_guide": -1}',  1, UNIX_TIMESTAMP() * 1000),
(101, 'Business LTD', 3, 49.00, '{"max_guides": -1, "cloud_storage": true,  "export_allowed": true,  "max_steps_per_guide": -1, "custom_branding": true}', 1, UNIX_TIMESTAMP() * 1000);

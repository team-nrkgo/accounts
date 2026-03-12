-- Centralized AI Module Schema

CREATE TABLE IF NOT EXISTS ai_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Context Identifiers
    org_id BIGINT NOT NULL,                   -- Organization that made the request
    user_id BIGINT,                           -- User who initiated (can be null for system tasks)
    product_module VARCHAR(50) NOT NULL,      -- The specific product asking for AI (e.g., 'ECHO', 'CRM', 'SYSTEM')
    
    -- Model Usage Details
    model_provider VARCHAR(50) NOT NULL,      -- e.g., 'GEMINI', 'OPENAI', 'ANTHROPIC'
    model_name VARCHAR(100) NOT NULL,         -- e.g., 'gemini-1.5-pro', 'gpt-4o'
    operation_type VARCHAR(100),              -- e.g., 'GENERATE_BLOG', 'SUMMARIZE_DOCUMENT'
    
    -- Token Tracking (Auditing and Billing)
    input_tokens INT DEFAULT 0,               -- Tokens sent in the prompt
    output_tokens INT DEFAULT 0,              -- Tokens generated in the response
    total_tokens INT DEFAULT 0,               -- Sum of input + output
    
    -- Performance Tracking
    execution_time_ms BIGINT,                 -- How long the AI took to respond
    status VARCHAR(20) DEFAULT 'SUCCESS',     -- 'SUCCESS', 'FAILED', 'TIMEOUT'
    error_message TEXT,                       -- If failed, the exact error from the provider
    
    created_time BIGINT,
    
    -- Indexes for quick aggregation and billing lookups
    INDEX idx_ai_audit_org (org_id, product_module),
    INDEX idx_ai_audit_user (user_id),
    INDEX idx_ai_audit_time (created_time)
);

-- Centralized Drive Module Schema

CREATE TABLE IF NOT EXISTS drive_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Increased to 100 to safely cover long tokens like the Zoho one!
    external_id VARCHAR(100) NOT NULL UNIQUE,  
    
    file_name VARCHAR(255) NOT NULL,          
    file_extension VARCHAR(10) NOT NULL,      
    storage_provider VARCHAR(50) NOT NULL,    
    
    file_size BIGINT,                         
    file_type VARCHAR(100),                   
    
    product_module VARCHAR(50) NOT NULL,      
    user_id BIGINT NOT NULL,                  
    org_id BIGINT NOT NULL,                   
    
    access_level ENUM('USER_PRIVATE', 'ORG_SHARED', 'PUBLIC') DEFAULT 'USER_PRIVATE',
    
    created_time BIGINT,
    INDEX idx_drive_org_user (org_id, user_id),
    INDEX idx_drive_product (product_module)
);

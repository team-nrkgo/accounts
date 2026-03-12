-- 1. Parent Table: Stores the Configuration and Identity
CREATE TABLE IF NOT EXISTS `job_arguments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `org_id` bigint NOT NULL,
  `product_code` int NOT NULL,              -- e.g., 2 for Echo
  `job_class` varchar(255) NOT NULL,        -- e.g., 'EchoPublishJob'
  `entity_id` bigint NOT NULL,               -- The ID of the Post/Document
  `arg_ref` varchar(255) DEFAULT NULL,      -- External ID or Correlation ID
  
  `user_id` bigint DEFAULT NULL,
  `job_type` int NOT NULL DEFAULT '0',      -- 0: Single, 1: Repeated
  `json_data` text,                         -- The task arguments
  `active_trigger_id` bigint DEFAULT NULL,  -- Pointer to the current LIVE trigger
  `status` int DEFAULT '0',                 -- 0: Active, 1: Paused/Inactive
  
  `created_by` bigint DEFAULT NULL,
  `created_time` bigint DEFAULT NULL,
  `modified_by` bigint DEFAULT NULL,
  `modified_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_identity` (`org_id`, `product_code`, `job_class`, `entity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Child Table: The Execution Queue (Triggers)
CREATE TABLE IF NOT EXISTS `job_triggers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_args_id` bigint NOT NULL,            -- FK to Parent
  `start_time` bigint NOT NULL,             -- Epoch timestamp for trigger
  `status` int NOT NULL DEFAULT '0',        -- 0: Waiting, 1: Running, 2: Finished, 3: Failed, 4: Cancelled
  `last_error` text,
  `retry_count` int DEFAULT '0',
  `thread_pool` varchar(50) DEFAULT 'default',
  `created_by` bigint DEFAULT NULL,
  `created_time` bigint DEFAULT NULL,
  `modified_by` bigint DEFAULT NULL,
  `modified_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_status_time` (`status`, `start_time`),
  CONSTRAINT `fk_job_args` FOREIGN KEY (`job_args_id`) REFERENCES `job_arguments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Rule Table: Logic for Repeating Jobs
CREATE TABLE IF NOT EXISTS `job_repeat_rules` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_args_id` bigint NOT NULL,
  `org_id` bigint DEFAULT NULL,
  `repeat_type` int NOT NULL,               -- e.g., 1: Daily, 2: Weekly, 3: Monthly
  `repeat_count` int DEFAULT '-1',          -- -1 for infinite entries
  `start_time` bigint DEFAULT NULL,         -- Cycle official start
  `end_time` bigint DEFAULT NULL,           -- Cycle official end
  `created_by` bigint DEFAULT NULL,
  `created_time` bigint DEFAULT NULL,
  `modified_by` bigint DEFAULT NULL,
  `modified_time` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_repeat_args` FOREIGN KEY (`job_args_id`) REFERENCES `job_arguments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(64) NULL,
    email VARCHAR(128) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(16) NOT NULL DEFAULT 'user',
    status VARCHAR(32) NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_users_email_deleted (email, is_deleted),
    UNIQUE KEY uk_users_username_deleted (username, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS login_logs (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NULL,
    ip VARCHAR(64) NULL,
    ua VARCHAR(255) NULL,
    success TINYINT(1) NOT NULL,
    reason VARCHAR(64) NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    KEY idx_login_logs_user_time (user_id, created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_roles_name_deleted (name, is_deleted),
    KEY idx_roles_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT PRIMARY KEY,
    code VARCHAR(128) NOT NULL,
    description VARCHAR(255) NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_permissions_code_deleted (code, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGINT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_role_permissions_role_permission_deleted (role_id, permission_id, is_deleted),
    KEY idx_role_permissions_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_user_roles_user_role_deleted (user_id, role_id, is_deleted),
    KEY idx_user_roles_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS plans (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(64) NOT NULL,
    billing_cycle VARCHAR(32) NOT NULL,
    quota_json TEXT NOT NULL,
    price BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_plans_code_deleted (code, is_deleted),
    KEY idx_plans_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    trial_end_at DATETIME(3) NULL,
    current_period_end DATETIME(3) NULL,
    effective_time DATETIME(3) NOT NULL,
    cancel_time DATETIME(3) NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    KEY idx_subscriptions_status (status),
    KEY idx_subscriptions_plan (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS request_logs (
    id BIGINT PRIMARY KEY,
    request_id VARCHAR(32) NOT NULL COMMENT '请求唯一标识',
    user_id BIGINT NULL COMMENT '操作用户ID',
    username VARCHAR(64) NULL COMMENT '操作用户名',
    method VARCHAR(8) NOT NULL COMMENT '请求方法',
    path VARCHAR(255) NOT NULL COMMENT '请求路径',
    query_string TEXT NULL COMMENT '查询参数',
    request_params TEXT NULL COMMENT '请求参数(JSON)',
    status_code INT NOT NULL COMMENT 'HTTP响应状态码',
    duration_ms INT NOT NULL COMMENT '执行耗时(毫秒)',
    client_ip VARCHAR(64) NULL COMMENT '客户端IP',
    user_agent VARCHAR(500) NULL COMMENT '浏览器标识',
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    KEY idx_request_logs_time (created_time),
    KEY idx_request_logs_user (user_id),
    KEY idx_request_logs_path (path),
    KEY idx_request_logs_status (status_code),
    KEY idx_request_logs_request_id (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请求日志表';

CREATE TABLE IF NOT EXISTS error_logs (
    id BIGINT PRIMARY KEY,
    request_id VARCHAR(32) NULL COMMENT '请求唯一标识',
    user_id BIGINT NULL COMMENT '操作用户ID',
    username VARCHAR(64) NULL COMMENT '操作用户名',
    request_path VARCHAR(255) NULL COMMENT '请求路径',
    request_params TEXT NULL COMMENT '请求参数(JSON)',
    client_ip VARCHAR(64) NULL COMMENT '客户端IP',
    exception_class VARCHAR(255) NOT NULL COMMENT '异常类名',
    exception_message TEXT NULL COMMENT '异常消息',
    stack_trace MEDIUMTEXT NULL COMMENT '完整堆栈信息',
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    KEY idx_error_logs_time (created_time),
    KEY idx_error_logs_user (user_id),
    KEY idx_error_logs_request_id (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常日志表';

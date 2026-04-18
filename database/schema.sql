
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

CREATE TABLE IF NOT EXISTS operation_logs (
    id BIGINT PRIMARY KEY,
    module VARCHAR(64) NOT NULL COMMENT '模块名称',
    action VARCHAR(64) NOT NULL COMMENT '操作类型',
    detail TEXT NULL COMMENT '操作详情',
    user_id BIGINT NULL COMMENT '操作用户ID',
    username VARCHAR(64) NULL COMMENT '操作用户名',
    client_ip VARCHAR(64) NULL COMMENT '客户端IP',
    request_id VARCHAR(32) NULL COMMENT '关联请求ID',
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    KEY idx_operation_logs_time (created_time),
    KEY idx_operation_logs_user (user_id),
    KEY idx_operation_logs_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

CREATE TABLE IF NOT EXISTS sys_dict_type (
    id BIGINT PRIMARY KEY,
    type_code VARCHAR(64) NOT NULL COMMENT '字典类型编码',
    type_name VARCHAR(64) NOT NULL COMMENT '字典类型名称',
    status VARCHAR(16) NOT NULL COMMENT '状态（ENABLED/DISABLED）',
    description VARCHAR(255) NULL COMMENT '描述',
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sys_dict_type_code_deleted (type_code, is_deleted),
    KEY idx_sys_dict_type_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

CREATE TABLE IF NOT EXISTS sys_dict_item (
    id BIGINT PRIMARY KEY,
    type_code VARCHAR(64) NOT NULL COMMENT '字典类型编码',
    item_label VARCHAR(128) NOT NULL COMMENT '字典项显示名',
    item_value VARCHAR(128) NOT NULL COMMENT '字典项值',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值（升序）',
    color VARCHAR(32) NULL COMMENT '前端显示颜色',
    ext_json TEXT NULL COMMENT '扩展字段 JSON',
    status VARCHAR(16) NOT NULL COMMENT '状态（ENABLED/DISABLED）',
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sys_dict_item_type_value_deleted (type_code, item_value, is_deleted),
    KEY idx_sys_dict_item_type_sort (type_code, sort),
    KEY idx_sys_dict_item_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典项表';

INSERT IGNORE INTO sys_dict_type (
    id, type_code, type_name, status, description,
    creator_id, creator_name, created_time, updater_id, updater_name, updated_time, is_deleted
) VALUES
    (1951000000000000001, 'admin_user_role', '管理端用户角色', 'ENABLED', '管理端用户角色字典', 0, 'system', NOW(3), 0, 'system', NOW(3), 0),
    (1951000000000000002, 'admin_user_status', '管理端用户状态', 'ENABLED', '管理端用户状态字典', 0, 'system', NOW(3), 0, 'system', NOW(3), 0),
    (1951000000000000003, 'plan_billing_cycle', '套餐计费周期', 'ENABLED', '套餐计费周期字典', 0, 'system', NOW(3), 0, 'system', NOW(3), 0),
    (1951000000000000004, 'plan_status', '套餐状态', 'ENABLED', '套餐状态字典', 0, 'system', NOW(3), 0, 'system', NOW(3), 0),
    (1951000000000000005, 'http_method', 'HTTP 请求方法', 'ENABLED', '请求日志筛选方法字典', 0, 'system', NOW(3), 0, 'system', NOW(3), 0);

INSERT IGNORE INTO sys_dict_item (
    id, type_code, item_label, item_value, sort, color, ext_json, status,
    creator_id, creator_name, created_time, updater_id, updater_name, updated_time, is_deleted
) VALUES
    (1951000000001000001, 'admin_user_role', '超管', 'super_admin', 10, 'red', NULL, 'ENABLED', 0, 'system', NOW(3), 0, 'system', NOW(3), 0),
    (1951000000001000002, 'admin_user_role', '管理员', 'admin', 20, 'processing', NULL, 'ENABLED', 0, 'system', NOW(3), 0, 'system', NOW(3), 0),
    (1951000000001000003, 'admin_user_role', '用户', 'user', 30, 'default', NULL, 'ENABLED', 0, 'system', NOW(3), 0, 'system', NOW(3), 0),

    (1951000000001000011, 'admin_user_status', '启用', 'ACTIVE', 10, 'success', NULL, 'ENABLED', 0, 'system', NOW(3), 0, 'system', NOW(3), 0),
    (1951000000001000012, 'admin_user_status', '禁用', 'INACTIVE', 20, 'default', NULL, 'ENABLED', 0, 'system', NOW(3), 0, 'system', NOW(3), 0),

    (1951000000001000021, 'plan_billing_cycle', '月付', 'MONTHLY', 10, 'processing', NULL, 'ENABLED', 0, 'system', NOW(3), 0, 'system', NOW(3), 0),
    (1951000000001000022, 'plan_billing_cycle', '年付', 'YEARLY', 20, 'success', NULL, 'ENABLED', 0, 'system', NOW(3), 0, 'system', NOW(3), 0),

    (1951000000001000031, 'plan_status', '启用', 'ACTIVE', 10, 'success', NULL, 'ENABLED', 0, 'system', NOW(3), 0, 'system', NOW(3), 0),
    (1951000000001000032, 'plan_status', '停用', 'INACTIVE', 20, 'default', NULL, 'ENABLED', 0, 'system', NOW(3), 0, 'system', NOW(3), 0),

    (1951000000001000041, 'http_method', 'GET', 'GET', 10, 'green', NULL, 'ENABLED', 0, 'system', NOW(3), 0, 'system', NOW(3), 0),
    (1951000000001000042, 'http_method', 'POST', 'POST', 20, 'blue', NULL, 'ENABLED', 0, 'system', NOW(3), 0, 'system', NOW(3), 0);

CREATE TABLE IF NOT EXISTS mail_channel (
    id BIGINT PRIMARY KEY,
    channel_code VARCHAR(64) NOT NULL COMMENT '通道编码',
    channel_name VARCHAR(64) NOT NULL COMMENT '通道名称',
    provider_type VARCHAR(16) NOT NULL DEFAULT 'SMTP' COMMENT 'SMTP/SES/SENDGRID',
    smtp_host VARCHAR(128) NOT NULL,
    smtp_port INT NOT NULL,
    smtp_username VARCHAR(128) NOT NULL,
    smtp_password_cipher VARCHAR(512) NOT NULL COMMENT '加密后的SMTP密码',
    tls_mode VARCHAR(16) NOT NULL DEFAULT 'STARTTLS' COMMENT 'NONE/STARTTLS/SSL',
    from_name VARCHAR(128) NOT NULL,
    from_email VARCHAR(128) NOT NULL,
    priority INT NOT NULL DEFAULT 0 COMMENT '越小优先级越高',
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    remark VARCHAR(255) NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_mail_channel_code_deleted (channel_code, is_deleted),
    KEY idx_mail_channel_status_priority (status, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件通道配置';

CREATE TABLE IF NOT EXISTS mail_template (
    id BIGINT PRIMARY KEY,
    template_code VARCHAR(64) NOT NULL COMMENT '模板编码',
    template_name VARCHAR(64) NOT NULL COMMENT '模板名称',
    biz_type VARCHAR(32) NOT NULL COMMENT 'VERIFY_CODE/SYSTEM_NOTICE',
    scene_code VARCHAR(64) NOT NULL COMMENT '业务场景',
    subject_template VARCHAR(255) NOT NULL COMMENT '主题模板',
    body_template MEDIUMTEXT NOT NULL COMMENT '正文模板',
    body_type VARCHAR(16) NOT NULL DEFAULT 'HTML' COMMENT 'HTML/TEXT',
    vars_schema_json TEXT NULL COMMENT '变量说明JSON',
    status VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    remark VARCHAR(255) NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_mail_template_code_deleted (template_code, is_deleted),
    KEY idx_mail_template_scene_status (biz_type, scene_code, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件模板';

CREATE TABLE IF NOT EXISTS mail_send_log (
    id BIGINT PRIMARY KEY,
    biz_type VARCHAR(32) NOT NULL COMMENT '业务类型',
    scene_code VARCHAR(64) NULL COMMENT '场景编码',
    template_code VARCHAR(64) NOT NULL COMMENT '模板编码',
    channel_code VARCHAR(64) NOT NULL COMMENT '通道编码',
    recipient_email VARCHAR(128) NOT NULL COMMENT '收件邮箱',
    subject_rendered VARCHAR(255) NOT NULL COMMENT '渲染主题',
    body_rendered MEDIUMTEXT NOT NULL COMMENT '渲染正文',
    biz_key VARCHAR(128) NULL COMMENT '业务幂等键',
    trace_id VARCHAR(64) NULL COMMENT '链路追踪ID',
    status VARCHAR(16) NOT NULL COMMENT 'PENDING/SUCCESS/FAILED',
    error_code VARCHAR(64) NULL COMMENT '错误码',
    error_message VARCHAR(512) NULL COMMENT '错误消息',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '已重试次数',
    max_retry_count INT NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    next_retry_time DATETIME(3) NULL COMMENT '下次重试时间',
    sent_time DATETIME(3) NULL COMMENT '发送时间',
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time DATETIME(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time DATETIME(3) NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_mail_send_log_biz_key_deleted (biz_key, is_deleted),
    KEY idx_mail_send_log_status_retry (status, next_retry_time),
    KEY idx_mail_send_log_template_time (template_code, created_time),
    KEY idx_mail_send_log_recipient_time (recipient_email, created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件发送日志';

INSERT IGNORE INTO mail_template (
    id, template_code, template_name, biz_type, scene_code, subject_template, body_template, body_type,
    vars_schema_json, status, remark, creator_id, creator_name, created_time, updater_id, updater_name, updated_time, is_deleted
) VALUES
    (1952000000000000101, 'vc_register', '注册验证码模板', 'VERIFY_CODE', 'REGISTER',
     '【Jay Boot】注册验证码', '<p>您好，您的注册验证码是 <b>{{code}}</b>，{{expireSeconds}} 秒内有效。</p>', 'HTML',
     '{"code":"string","expireSeconds":"number"}', 'ENABLED', '默认注册验证码模板', 0, 'system', NOW(3), 0, 'system', NOW(3), 0),
    (1952000000000000102, 'vc_login_verify', '登录验证验证码模板', 'VERIFY_CODE', 'LOGIN_VERIFY',
     '【Jay Boot】登录验证码', '<p>您好，您的登录验证码是 <b>{{code}}</b>，{{expireSeconds}} 秒内有效。</p>', 'HTML',
     '{"code":"string","expireSeconds":"number"}', 'ENABLED', '默认登录验证码模板', 0, 'system', NOW(3), 0, 'system', NOW(3), 0),
    (1952000000000000103, 'vc_reset_password', '找回密码验证码模板', 'VERIFY_CODE', 'RESET_PASSWORD',
     '【Jay Boot】找回密码验证码', '<p>您好，您的找回密码验证码是 <b>{{code}}</b>，{{expireSeconds}} 秒内有效。</p>', 'HTML',
     '{"code":"string","expireSeconds":"number"}', 'ENABLED', '默认找回密码验证码模板', 0, 'system', NOW(3), 0, 'system', NOW(3), 0),
    (1952000000000000104, 'vc_change_email', '修改邮箱验证码模板', 'VERIFY_CODE', 'CHANGE_EMAIL',
     '【Jay Boot】修改邮箱验证码', '<p>您好，您的修改邮箱验证码是 <b>{{code}}</b>，{{expireSeconds}} 秒内有效。</p>', 'HTML',
     '{"code":"string","expireSeconds":"number"}', 'ENABLED', '默认修改邮箱验证码模板', 0, 'system', NOW(3), 0, 'system', NOW(3), 0),
    (1952000000000000199, 'system_notice_default', '系统通知模板', 'SYSTEM_NOTICE', 'SYSTEM_NOTICE',
     '【Jay Boot】系统通知', '<p>{{content}}</p>', 'HTML',
     '{"content":"string"}', 'ENABLED', '默认系统通知模板', 0, 'system', NOW(3), 0, 'system', NOW(3), 0);

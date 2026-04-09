CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    email VARCHAR(128) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time TIMESTAMP(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time TIMESTAMP(3) NOT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email_deleted ON users(email, is_deleted);

CREATE TABLE IF NOT EXISTS login_logs (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    user_id BIGINT,
    ip VARCHAR(64),
    ua VARCHAR(255),
    success BOOLEAN NOT NULL,
    reason VARCHAR(64),
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time TIMESTAMP(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time TIMESTAMP(3) NOT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS tenants (
    id BIGINT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    owner_user_id BIGINT NOT NULL,
    plan_code VARCHAR(32) NOT NULL DEFAULT 'FREE',
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time TIMESTAMP(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time TIMESTAMP(3) NOT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_tenants_name_deleted ON tenants(name, is_deleted);
CREATE INDEX IF NOT EXISTS idx_tenants_owner_user_id ON tenants(owner_user_id);

CREATE TABLE IF NOT EXISTS user_tenant (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_in_tenant VARCHAR(32) NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time TIMESTAMP(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time TIMESTAMP(3) NOT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_tenant_user_id_deleted ON user_tenant(user_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_user_tenant_tenant_id ON user_tenant(tenant_id);

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time TIMESTAMP(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time TIMESTAMP(3) NOT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_roles_tenant_name_deleted ON roles(tenant_id, name, is_deleted);
CREATE INDEX IF NOT EXISTS idx_roles_tenant_id ON roles(tenant_id);

CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT PRIMARY KEY,
    code VARCHAR(128) NOT NULL,
    description VARCHAR(255) NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time TIMESTAMP(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time TIMESTAMP(3) NOT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_permissions_code_deleted ON permissions(code, is_deleted);

CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time TIMESTAMP(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time TIMESTAMP(3) NOT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_role_permissions_tenant_role_permission_deleted
    ON role_permissions(tenant_id, role_id, permission_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_role_permissions_tenant_role ON role_permissions(tenant_id, role_id);

CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time TIMESTAMP(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time TIMESTAMP(3) NOT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_roles_tenant_user_role_deleted
    ON user_roles(tenant_id, user_id, role_id, is_deleted);
CREATE INDEX IF NOT EXISTS idx_user_roles_tenant_user ON user_roles(tenant_id, user_id);

CREATE TABLE IF NOT EXISTS plans (
    id BIGINT PRIMARY KEY,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(64) NOT NULL,
    billing_cycle VARCHAR(32) NOT NULL,
    quota_json CLOB NOT NULL,
    price BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time TIMESTAMP(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time TIMESTAMP(3) NOT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_plans_code_deleted ON plans(code, is_deleted);
CREATE INDEX IF NOT EXISTS idx_plans_status ON plans(status);

CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    trial_end_at TIMESTAMP(3),
    current_period_end TIMESTAMP(3),
    effective_time TIMESTAMP(3) NOT NULL,
    cancel_time TIMESTAMP(3),
    creator_id BIGINT NOT NULL,
    creator_name VARCHAR(64) NOT NULL,
    created_time TIMESTAMP(3) NOT NULL,
    updater_id BIGINT NOT NULL,
    updater_name VARCHAR(64) NOT NULL,
    updated_time TIMESTAMP(3) NOT NULL,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_subscriptions_tenant_status ON subscriptions(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_subscriptions_tenant_plan ON subscriptions(tenant_id, plan_id);

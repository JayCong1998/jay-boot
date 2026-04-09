-- Seed data for RBAC + tenant + billing bootstrap (MySQL 8.0)
-- Idempotent: safe to run multiple times.
-- Admin credential:
--   email: admin@jayboot.local
--   password: Admin1234

SET @seed_now = NOW(3);

SET @tenant_id = 900000000000000001;
SET @admin_user_id = 900000000000000101;
SET @owner_role_id = 900000000000000201;
SET @plan_free_id = 900000000000000401;
SET @plan_pro_id = 900000000000000402;
SET @subscription_id = 900000000000000501;

SET @perm_role_read_id = 900000000000000301;
SET @perm_role_write_id = 900000000000000302;
SET @perm_role_grant_id = 900000000000000303;
SET @perm_user_grant_id = 900000000000000304;
SET @perm_billing_plan_read_id = 900000000000000305;
SET @perm_billing_sub_create_id = 900000000000000306;
SET @perm_billing_sub_update_id = 900000000000000307;

SET @seed_actor_id = 0;
SET @seed_actor_name = 'seed-script';

INSERT INTO tenants (
    id, name, owner_user_id, plan_code,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT
    @tenant_id, 'system-admin-tenant', @admin_user_id, 'FREE',
    @seed_actor_id, @seed_actor_name, @seed_now,
    @seed_actor_id, @seed_actor_name, @seed_now, 0
WHERE NOT EXISTS (
    SELECT 1 FROM tenants t
    WHERE t.id = @tenant_id OR (t.name = 'system-admin-tenant' AND t.is_deleted = 0)
);

INSERT INTO users (
    id, tenant_id, email, password_hash, status,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT
    @admin_user_id, @tenant_id, 'admin@jayboot.local',
    '$2a$10$Kc3XXUldiZ08.e8DAuyT/OWFwDJFBN5KWMr6/81JL5kjfzggETrsC', 'ACTIVE',
    @seed_actor_id, @seed_actor_name, @seed_now,
    @seed_actor_id, @seed_actor_name, @seed_now, 0
WHERE NOT EXISTS (
    SELECT 1 FROM users u
    WHERE u.id = @admin_user_id OR (u.email = 'admin@jayboot.local' AND u.is_deleted = 0)
);

UPDATE users
SET tenant_id = @tenant_id,
    status = 'ACTIVE',
    updater_id = @seed_actor_id,
    updater_name = @seed_actor_name,
    updated_time = @seed_now
WHERE email = 'admin@jayboot.local' AND is_deleted = 0;

UPDATE tenants
SET owner_user_id = @admin_user_id,
    plan_code = 'FREE',
    updater_id = @seed_actor_id,
    updater_name = @seed_actor_name,
    updated_time = @seed_now
WHERE id = @tenant_id AND is_deleted = 0;

INSERT INTO user_tenant (
    id, tenant_id, user_id, role_in_tenant,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT
    900000000000000111, @tenant_id, @admin_user_id, 'OWNER',
    @seed_actor_id, @seed_actor_name, @seed_now,
    @seed_actor_id, @seed_actor_name, @seed_now, 0
WHERE NOT EXISTS (
    SELECT 1 FROM user_tenant ut
    WHERE ut.user_id = @admin_user_id AND ut.is_deleted = 0
);

INSERT INTO roles (
    id, tenant_id, name,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT
    @owner_role_id, @tenant_id, 'OWNER',
    @seed_actor_id, @seed_actor_name, @seed_now,
    @seed_actor_id, @seed_actor_name, @seed_now, 0
WHERE NOT EXISTS (
    SELECT 1 FROM roles r
    WHERE r.id = @owner_role_id OR (r.tenant_id = @tenant_id AND r.name = 'OWNER' AND r.is_deleted = 0)
);

INSERT INTO permissions (
    id, code, description,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT @perm_role_read_id, 'rbac.role.read', '查看角色',
       @seed_actor_id, @seed_actor_name, @seed_now,
       @seed_actor_id, @seed_actor_name, @seed_now, 0
WHERE NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.code = 'rbac.role.read' AND p.is_deleted = 0
);

INSERT INTO permissions (
    id, code, description,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT @perm_role_write_id, 'rbac.role.write', '创建角色',
       @seed_actor_id, @seed_actor_name, @seed_now,
       @seed_actor_id, @seed_actor_name, @seed_now, 0
WHERE NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.code = 'rbac.role.write' AND p.is_deleted = 0
);

INSERT INTO permissions (
    id, code, description,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT @perm_role_grant_id, 'rbac.role.grant', '为角色分配权限',
       @seed_actor_id, @seed_actor_name, @seed_now,
       @seed_actor_id, @seed_actor_name, @seed_now, 0
WHERE NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.code = 'rbac.role.grant' AND p.is_deleted = 0
);

INSERT INTO permissions (
    id, code, description,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT @perm_user_grant_id, 'rbac.user.grant', '为用户分配角色',
       @seed_actor_id, @seed_actor_name, @seed_now,
       @seed_actor_id, @seed_actor_name, @seed_now, 0
WHERE NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.code = 'rbac.user.grant' AND p.is_deleted = 0
);

INSERT INTO permissions (
    id, code, description,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT @perm_billing_plan_read_id, 'billing.plan.read', '查看套餐',
       @seed_actor_id, @seed_actor_name, @seed_now,
       @seed_actor_id, @seed_actor_name, @seed_now, 0
WHERE NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.code = 'billing.plan.read' AND p.is_deleted = 0
);

INSERT INTO permissions (
    id, code, description,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT @perm_billing_sub_create_id, 'billing.subscription.create', '创建订阅',
       @seed_actor_id, @seed_actor_name, @seed_now,
       @seed_actor_id, @seed_actor_name, @seed_now, 0
WHERE NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.code = 'billing.subscription.create' AND p.is_deleted = 0
);

INSERT INTO permissions (
    id, code, description,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT @perm_billing_sub_update_id, 'billing.subscription.update', '更新订阅',
       @seed_actor_id, @seed_actor_name, @seed_now,
       @seed_actor_id, @seed_actor_name, @seed_now, 0
WHERE NOT EXISTS (
    SELECT 1 FROM permissions p WHERE p.code = 'billing.subscription.update' AND p.is_deleted = 0
);

UPDATE permissions
SET description = CASE code
    WHEN 'rbac.role.read' THEN '查看角色'
    WHEN 'rbac.role.write' THEN '创建角色'
    WHEN 'rbac.role.grant' THEN '为角色分配权限'
    WHEN 'rbac.user.grant' THEN '为用户分配角色'
    WHEN 'billing.plan.read' THEN '查看套餐'
    WHEN 'billing.subscription.create' THEN '创建订阅'
    WHEN 'billing.subscription.update' THEN '更新订阅'
    ELSE description
END,
    updater_id = @seed_actor_id,
    updater_name = @seed_actor_name,
    updated_time = @seed_now
WHERE code IN (
    'rbac.role.read',
    'rbac.role.write',
    'rbac.role.grant',
    'rbac.user.grant',
    'billing.plan.read',
    'billing.subscription.create',
    'billing.subscription.update'
) AND is_deleted = 0;

INSERT INTO role_permissions (
    id, tenant_id, role_id, permission_id,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT
    900000000000000601, @tenant_id, @owner_role_id, p.id,
    @seed_actor_id, @seed_actor_name, @seed_now,
    @seed_actor_id, @seed_actor_name, @seed_now, 0
FROM permissions p
WHERE p.code = 'rbac.role.read' AND p.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.tenant_id = @tenant_id
        AND rp.role_id = @owner_role_id
        AND rp.permission_id = p.id
        AND rp.is_deleted = 0
  );

INSERT INTO role_permissions (
    id, tenant_id, role_id, permission_id,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT
    900000000000000602, @tenant_id, @owner_role_id, p.id,
    @seed_actor_id, @seed_actor_name, @seed_now,
    @seed_actor_id, @seed_actor_name, @seed_now, 0
FROM permissions p
WHERE p.code = 'rbac.role.write' AND p.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.tenant_id = @tenant_id
        AND rp.role_id = @owner_role_id
        AND rp.permission_id = p.id
        AND rp.is_deleted = 0
  );

INSERT INTO role_permissions (
    id, tenant_id, role_id, permission_id,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT
    900000000000000603, @tenant_id, @owner_role_id, p.id,
    @seed_actor_id, @seed_actor_name, @seed_now,
    @seed_actor_id, @seed_actor_name, @seed_now, 0
FROM permissions p
WHERE p.code = 'rbac.role.grant' AND p.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.tenant_id = @tenant_id
        AND rp.role_id = @owner_role_id
        AND rp.permission_id = p.id
        AND rp.is_deleted = 0
  );

INSERT INTO role_permissions (
    id, tenant_id, role_id, permission_id,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT
    900000000000000604, @tenant_id, @owner_role_id, p.id,
    @seed_actor_id, @seed_actor_name, @seed_now,
    @seed_actor_id, @seed_actor_name, @seed_now, 0
FROM permissions p
WHERE p.code = 'rbac.user.grant' AND p.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.tenant_id = @tenant_id
        AND rp.role_id = @owner_role_id
        AND rp.permission_id = p.id
        AND rp.is_deleted = 0
  );

INSERT INTO role_permissions (
    id, tenant_id, role_id, permission_id,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT
    900000000000000605, @tenant_id, @owner_role_id, p.id,
    @seed_actor_id, @seed_actor_name, @seed_now,
    @seed_actor_id, @seed_actor_name, @seed_now, 0
FROM permissions p
WHERE p.code = 'billing.plan.read' AND p.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.tenant_id = @tenant_id
        AND rp.role_id = @owner_role_id
        AND rp.permission_id = p.id
        AND rp.is_deleted = 0
  );

INSERT INTO role_permissions (
    id, tenant_id, role_id, permission_id,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT
    900000000000000606, @tenant_id, @owner_role_id, p.id,
    @seed_actor_id, @seed_actor_name, @seed_now,
    @seed_actor_id, @seed_actor_name, @seed_now, 0
FROM permissions p
WHERE p.code = 'billing.subscription.create' AND p.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.tenant_id = @tenant_id
        AND rp.role_id = @owner_role_id
        AND rp.permission_id = p.id
        AND rp.is_deleted = 0
  );

INSERT INTO role_permissions (
    id, tenant_id, role_id, permission_id,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT
    900000000000000607, @tenant_id, @owner_role_id, p.id,
    @seed_actor_id, @seed_actor_name, @seed_now,
    @seed_actor_id, @seed_actor_name, @seed_now, 0
FROM permissions p
WHERE p.code = 'billing.subscription.update' AND p.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.tenant_id = @tenant_id
        AND rp.role_id = @owner_role_id
        AND rp.permission_id = p.id
        AND rp.is_deleted = 0
  );

INSERT INTO user_roles (
    id, tenant_id, user_id, role_id,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT
    900000000000000701, @tenant_id, @admin_user_id, @owner_role_id,
    @seed_actor_id, @seed_actor_name, @seed_now,
    @seed_actor_id, @seed_actor_name, @seed_now, 0
WHERE NOT EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.tenant_id = @tenant_id
      AND ur.user_id = @admin_user_id
      AND ur.role_id = @owner_role_id
      AND ur.is_deleted = 0
);

INSERT INTO plans (
    id, code, name, billing_cycle, quota_json, price, status,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT
    @plan_free_id, 'FREE', '免费版', 'MONTHLY',
    '{"maxUsers":3,"maxProjects":3,"apiRatePerMin":30}', 0, 'ACTIVE',
    @seed_actor_id, @seed_actor_name, @seed_now,
    @seed_actor_id, @seed_actor_name, @seed_now, 0
WHERE NOT EXISTS (
    SELECT 1 FROM plans p WHERE p.code = 'FREE' AND p.is_deleted = 0
);

INSERT INTO plans (
    id, code, name, billing_cycle, quota_json, price, status,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT
    @plan_pro_id, 'PRO', '专业版', 'MONTHLY',
    '{"maxUsers":50,"maxProjects":100,"apiRatePerMin":600}', 9900, 'ACTIVE',
    @seed_actor_id, @seed_actor_name, @seed_now,
    @seed_actor_id, @seed_actor_name, @seed_now, 0
WHERE NOT EXISTS (
    SELECT 1 FROM plans p WHERE p.code = 'PRO' AND p.is_deleted = 0
);

UPDATE plans
SET status = 'ACTIVE',
    updater_id = @seed_actor_id,
    updater_name = @seed_actor_name,
    updated_time = @seed_now
WHERE code IN ('FREE', 'PRO') AND is_deleted = 0;

INSERT INTO subscriptions (
    id, tenant_id, plan_id, status,
    trial_end_at, current_period_end, effective_time, cancel_time,
    creator_id, creator_name, created_time,
    updater_id, updater_name, updated_time, is_deleted
)
SELECT
    @subscription_id, @tenant_id,
    (SELECT p.id FROM plans p WHERE p.code = 'FREE' AND p.is_deleted = 0 LIMIT 1),
    'ACTIVE',
    NULL,
    DATE_ADD(@seed_now, INTERVAL 1 MONTH),
    @seed_now,
    NULL,
    @seed_actor_id, @seed_actor_name, @seed_now,
    @seed_actor_id, @seed_actor_name, @seed_now, 0
WHERE NOT EXISTS (
    SELECT 1 FROM subscriptions s
    WHERE s.tenant_id = @tenant_id
      AND s.status IN ('TRIALING', 'ACTIVE', 'PAST_DUE')
      AND s.is_deleted = 0
);

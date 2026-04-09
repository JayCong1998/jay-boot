package com.jaycong.boot.modules.rbac.service;

import java.util.List;

public final class RbacPermissionCatalog {

    public static final String ROLE_READ = "rbac.role.read";
    public static final String ROLE_WRITE = "rbac.role.write";
    public static final String ROLE_GRANT = "rbac.role.grant";
    public static final String USER_GRANT = "rbac.user.grant";
    public static final String BILLING_PLAN_READ = "billing.plan.read";
    public static final String BILLING_SUBSCRIPTION_CREATE = "billing.subscription.create";
    public static final String BILLING_SUBSCRIPTION_UPDATE = "billing.subscription.update";
    public static final String OWNER_ROLE_NAME = "OWNER";

    private RbacPermissionCatalog() {
    }

    public static List<PermissionDefinition> builtInPermissions() {
        return List.of(
                new PermissionDefinition(ROLE_READ, "查看角色"),
                new PermissionDefinition(ROLE_WRITE, "创建角色"),
                new PermissionDefinition(ROLE_GRANT, "为角色分配权限"),
                new PermissionDefinition(USER_GRANT, "为用户分配角色"),
                new PermissionDefinition(BILLING_PLAN_READ, "查看套餐"),
                new PermissionDefinition(BILLING_SUBSCRIPTION_CREATE, "创建订阅"),
                new PermissionDefinition(BILLING_SUBSCRIPTION_UPDATE, "更新订阅")
        );
    }

    public record PermissionDefinition(String code, String description) {
    }
}

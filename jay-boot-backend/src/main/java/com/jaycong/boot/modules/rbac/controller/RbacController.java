package com.jaycong.boot.modules.rbac.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.modules.rbac.dto.PermissionCheckResponse;
import com.jaycong.boot.modules.rbac.dto.RoleAssignPermissionsRequest;
import com.jaycong.boot.modules.rbac.dto.RoleCreateRequest;
import com.jaycong.boot.modules.rbac.dto.RoleResponse;
import com.jaycong.boot.modules.rbac.dto.UserAssignRolesRequest;
import com.jaycong.boot.modules.rbac.service.RbacPermissionCatalog;
import com.jaycong.boot.modules.rbac.service.RbacService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * RBAC 控制器，提供角色、授权与用户角色分配能力。
 */
@RestController
@RequestMapping("/api/rbac")
@Validated
@Tag(name = "RBAC 管理", description = "角色、权限与用户角色分配")
public class RbacController {

    private final RbacService rbacService;

    public RbacController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    /**
     * 查询当前租户角色列表。
     */
    @Operation(summary = "查询角色列表")
    @SaCheckPermission(RbacPermissionCatalog.ROLE_READ)
    @GetMapping("/roles")
    public ApiResponse<List<RoleResponse>> roles() {
        return ApiResponse.success(rbacService.listCurrentTenantRoles());
    }

    /**
     * 在当前租户下创建角色。
     */
    @Operation(summary = "创建角色")
    @SaCheckPermission(RbacPermissionCatalog.ROLE_WRITE)
    @PostMapping("/roles")
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody RoleCreateRequest request) {
        return ApiResponse.success(rbacService.createRole(request));
    }

    /**
     * 覆盖式设置指定角色的权限编码列表。
     */
    @Operation(summary = "角色分配权限")
    @SaCheckPermission(RbacPermissionCatalog.ROLE_GRANT)
    @PostMapping("/roles/{id}/permissions")
    public ApiResponse<Void> assignPermissions(@PathVariable("id") Long roleId,
                                               @Valid @RequestBody RoleAssignPermissionsRequest request) {
        rbacService.assignPermissions(roleId, request);
        return ApiResponse.success(null);
    }

    /**
     * 覆盖式设置指定用户的角色列表。
     */
    @Operation(summary = "用户分配角色")
    @SaCheckPermission(RbacPermissionCatalog.USER_GRANT)
    @PostMapping("/users/{id}/roles")
    public ApiResponse<Void> assignUserRoles(@PathVariable("id") Long userId,
                                             @Valid @RequestBody UserAssignRolesRequest request) {
        rbacService.assignRolesToUser(userId, request);
        return ApiResponse.success(null);
    }

    /**
     * 检查当前登录用户是否拥有指定权限。
     */
    @Operation(summary = "检查当前用户权限")
    @GetMapping("/permissions/check")
    public ApiResponse<PermissionCheckResponse> checkPermission(
            @RequestParam("code") @NotBlank(message = "权限编码不能为空") String code) {
        return ApiResponse.success(rbacService.checkCurrentUserPermission(code));
    }
}

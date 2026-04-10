package com.jaycong.boot.rest.admin.controller;

import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.modules.auth.dto.AdminUserCreateRequest;
import com.jaycong.boot.modules.auth.dto.AdminUserPageRequest;
import com.jaycong.boot.modules.auth.dto.AdminUserPageResponse;
import com.jaycong.boot.modules.auth.dto.AdminUserPasswordResetRequest;
import com.jaycong.boot.modules.auth.dto.AdminUserRole;
import com.jaycong.boot.modules.auth.dto.AdminUserStatus;
import com.jaycong.boot.modules.auth.dto.AdminUserStatusUpdateRequest;
import com.jaycong.boot.modules.auth.dto.AdminUserUpdateRequest;
import com.jaycong.boot.modules.auth.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@Validated
@Tag(name = "管理端用户管理", description = "用户列表、创建、编辑、启停用、重置密码")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Operation(summary = "分页查询用户")
    @GetMapping
    public ApiResponse<AdminUserPageResponse> page(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AdminUserRole role,
            @RequestParam(required = false) AdminUserStatus status) {
        return ApiResponse.success(adminUserService.page(new AdminUserPageRequest(page, pageSize, keyword, role, status)));
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody AdminUserCreateRequest request) {
        adminUserService.create(request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody AdminUserUpdateRequest request) {
        adminUserService.update(id, request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "更新用户状态")
    @PostMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody AdminUserStatusUpdateRequest request) {
        adminUserService.updateStatus(id, request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "重置用户密码")
    @PostMapping("/{id}/password/reset")
    public ApiResponse<Void> resetPassword(@PathVariable Long id,
                                           @Valid @RequestBody AdminUserPasswordResetRequest request) {
        adminUserService.resetPassword(id, request);
        return ApiResponse.success(null);
    }
}


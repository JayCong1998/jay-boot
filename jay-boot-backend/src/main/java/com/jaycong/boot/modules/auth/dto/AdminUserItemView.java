package com.jaycong.boot.modules.auth.dto;

import com.jaycong.boot.common.constant.enums.AdminUserRole;
import com.jaycong.boot.common.constant.enums.AdminUserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理端用户列表项")
public record AdminUserItemView(
        @Schema(description = "用户 ID", example = "1001")
        Long id,
        @Schema(description = "用户名", example = "alice")
        String username,
        @Schema(description = "邮箱", example = "alice@example.com")
        String email,
        @Schema(description = "角色")
        AdminUserRole role,
        @Schema(description = "状态")
        AdminUserStatus status,
        @Schema(description = "创建时间", example = "2026-04-10T16:30:00")
        String createdTime
) {
}


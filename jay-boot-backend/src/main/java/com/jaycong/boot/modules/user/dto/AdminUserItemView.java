package com.jaycong.boot.modules.user.dto;

import com.jaycong.boot.common.constant.enums.AdminUserRole;
import com.jaycong.boot.common.constant.enums.AdminUserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Admin user list item")
public record AdminUserItemView(
        @Schema(description = "User ID serialized as string", example = "1001", type = "string")
        Long id,
        @Schema(description = "Username", example = "alice")
        String username,
        @Schema(description = "Email", example = "alice@example.com")
        String email,
        @Schema(description = "Role")
        AdminUserRole role,
        @Schema(description = "Status")
        AdminUserStatus status,
        @Schema(description = "Created time", example = "2026-04-10T16:30:00")
        String createdTime
) {
}

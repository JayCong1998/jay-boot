package com.jaycong.boot.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "登录用户信息")
public record AuthUserView(
        @Schema(description = "用户 ID", example = "1001")
        Long id,
        @Schema(description = "租户 ID", example = "2001")
        Long tenantId,
        @Schema(description = "邮箱", example = "user@example.com")
        String email,
        @Schema(description = "用户状态", example = "ACTIVE")
        String status
) {
}

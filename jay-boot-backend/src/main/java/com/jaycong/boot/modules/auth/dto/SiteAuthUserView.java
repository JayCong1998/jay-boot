package com.jaycong.boot.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户端当前用户信息")
public record SiteAuthUserView(
        @Schema(description = "用户 ID", example = "2")
        Long id,
        @Schema(description = "用户名", example = "creator01")
        String username,
        @Schema(description = "邮箱", example = "creator01@example.com")
        String email,
        @Schema(description = "创建时间（ISO-8601）", example = "2026-04-09T08:00:00.000Z")
        String createdAt
) {
}

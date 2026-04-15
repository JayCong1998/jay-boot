package com.jaycong.boot.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current site user")
public record SiteAuthUserView(
        @Schema(description = "User ID serialized as string", example = "2", type = "string")
        Long id,
        @Schema(description = "Username", example = "creator01")
        String username,
        @Schema(description = "Email", example = "creator01@example.com")
        String email,
        @Schema(description = "Created time in ISO-8601", example = "2026-04-09T08:00:00.000Z")
        String createdAt
) {
}

package com.jaycong.boot.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authenticated user")
public record AuthUserView(
        @Schema(description = "User ID serialized as string", example = "1001", type = "string")
        Long id,
        @Schema(description = "Username", example = "creator01")
        String username,
        @Schema(description = "Email", example = "user@example.com")
        String email,
        @Schema(description = "Status", example = "ACTIVE")
        String status
) {
}

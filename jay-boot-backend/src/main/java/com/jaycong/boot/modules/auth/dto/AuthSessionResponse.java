package com.jaycong.boot.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current admin auth session")
public record AuthSessionResponse(
        @Schema(description = "Login user ID serialized as string", type = "string")
        Long loginId,
        @Schema(description = "Current token")
        String token,
        @Schema(description = "Token TTL in seconds")
        long tokenTimeout,
        @Schema(description = "Current user info")
        AuthUserView user
) {
}

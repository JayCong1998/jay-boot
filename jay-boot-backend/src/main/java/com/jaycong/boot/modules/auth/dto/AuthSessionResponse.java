package com.jaycong.boot.modules.auth.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current admin auth session")
public record AuthSessionResponse(
        @Schema(description = "Login user ID serialized as string", type = "string")
        @JsonSerialize(using = ToStringSerializer.class)
        Long loginId,
        @Schema(description = "Current token")
        String token,
        @Schema(description = "Token TTL in seconds")
        long tokenTimeout,
        @Schema(description = "Current user info")
        AuthUserView user
) {
}

package com.jaycong.boot.modules.auth.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authenticated user")
public record AuthUserView(
        @Schema(description = "User ID serialized as string", example = "1001", type = "string")
        @JsonSerialize(using = ToStringSerializer.class)
        Long id,
        @Schema(description = "Email", example = "user@example.com")
        String email,
        @Schema(description = "Status", example = "ACTIVE")
        String status
) {
}

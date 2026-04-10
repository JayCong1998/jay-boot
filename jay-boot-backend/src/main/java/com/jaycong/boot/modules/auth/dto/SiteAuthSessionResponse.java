package com.jaycong.boot.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户端会话响应")
public record SiteAuthSessionResponse(
        @Schema(description = "访问令牌")
        String token,
        @Schema(description = "当前用户信息")
        SiteAuthUserView user
) {
}

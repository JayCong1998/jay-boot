package com.jaycong.boot.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "当前会话响应")
public record AuthSessionResponse(
        @Schema(description = "登录用户 ID")
        Long loginId,
        @Schema(description = "当前令牌")
        String token,
        @Schema(description = "令牌有效期（秒）")
        long tokenTimeout,
        @Schema(description = "当前登录用户信息")
        AuthUserView user
) {
}

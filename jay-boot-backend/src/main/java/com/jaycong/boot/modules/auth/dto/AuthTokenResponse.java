package com.jaycong.boot.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "认证令牌响应")
public record AuthTokenResponse(
        @Schema(description = "访问令牌")
        String token,
        @Schema(description = "令牌有效期（秒）")
        long tokenTimeout,
        @Schema(description = "当前登录用户信息")
        AuthUserView user
) {
}

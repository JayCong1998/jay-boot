package com.jaycong.boot.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "用户端登出请求")
public record SiteLogoutRequest(
        @Schema(description = "会话令牌", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "token 不能为空")
        String token
) {
}

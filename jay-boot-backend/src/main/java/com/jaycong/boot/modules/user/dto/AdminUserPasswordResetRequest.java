package com.jaycong.boot.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "管理端重置用户密码请求")
public record AdminUserPasswordResetRequest(
        @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "abc123")
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 10, message = "密码长度必须在 6 到 10 位之间")
        String newPassword
) {
}


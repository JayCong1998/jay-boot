package com.jaycong.boot.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "修改密码请求")
public record ChangePasswordRequest(
        @Schema(description = "旧密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "Password123")
        @NotBlank(message = "旧密码不能为空")
        String oldPassword,
        @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "Password456")
        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, max = 64, message = "密码长度必须在 8 到 64 位之间")
        String newPassword
) {
}

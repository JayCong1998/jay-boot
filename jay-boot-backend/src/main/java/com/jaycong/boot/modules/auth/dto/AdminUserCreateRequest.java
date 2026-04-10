package com.jaycong.boot.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "管理端创建用户请求")
public record AdminUserCreateRequest(
        @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "alice")
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 24, message = "用户名长度必须在 3 到 24 位之间")
        String username,
        @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "alice@example.com")
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String email,
        @Schema(description = "角色", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "角色不能为空")
        AdminUserRole role,
        @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "abc123")
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 10, message = "密码长度必须在 6 到 10 位之间")
        String password,
        @Schema(description = "状态，默认 ACTIVE")
        AdminUserStatus status
) {
}


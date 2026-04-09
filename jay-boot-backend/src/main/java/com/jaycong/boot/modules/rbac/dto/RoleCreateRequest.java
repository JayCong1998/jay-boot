package com.jaycong.boot.modules.rbac.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "角色创建请求")
public record RoleCreateRequest(
        @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "MANAGER")
        @NotBlank(message = "角色名称不能为空")
        @Size(min = 2, max = 64, message = "角色名称长度必须在 2 到 64 个字符之间")
        String name
) {
}

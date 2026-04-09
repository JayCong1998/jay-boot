package com.jaycong.boot.modules.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "更新当前租户请求")
public record TenantUpdateCurrentRequest(
        @Schema(description = "工作区名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "Acme Workspace")
        @NotBlank(message = "工作区名称不能为空")
        @Size(min = 2, max = 64, message = "工作区名称长度必须在 2 到 64 个字符之间")
        String name
) {
}

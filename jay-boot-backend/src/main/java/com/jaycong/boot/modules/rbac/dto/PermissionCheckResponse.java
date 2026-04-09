package com.jaycong.boot.modules.rbac.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "权限检查响应")
public record PermissionCheckResponse(
        @Schema(description = "权限编码", example = "rbac.role.read")
        String code,
        @Schema(description = "是否已授予")
        boolean granted
) {
}

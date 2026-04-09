package com.jaycong.boot.modules.rbac.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "角色响应")
public record RoleResponse(
        @Schema(description = "角色 ID", example = "3001")
        Long roleId,
        @Schema(description = "角色名称", example = "MANAGER")
        String name
) {
}

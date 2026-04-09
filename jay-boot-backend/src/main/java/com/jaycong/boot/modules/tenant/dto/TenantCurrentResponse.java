package com.jaycong.boot.modules.tenant.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "当前租户信息")
public record TenantCurrentResponse(
        @Schema(description = "租户 ID", example = "2001")
        Long tenantId,
        @Schema(description = "工作区名称", example = "workspace-a1b2")
        String name,
        @Schema(description = "租户拥有者用户 ID", example = "1001")
        Long ownerUserId,
        @Schema(description = "套餐编码", example = "FREE")
        String planCode
) {
}

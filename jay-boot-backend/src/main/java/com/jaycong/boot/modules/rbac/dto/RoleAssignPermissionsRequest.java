package com.jaycong.boot.modules.rbac.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "角色权限分配请求")
public record RoleAssignPermissionsRequest(
        @Schema(description = "权限编码列表", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "权限编码列表不能为空")
        List<@NotBlank(message = "权限编码不能为空") String> permissionCodes
) {
}

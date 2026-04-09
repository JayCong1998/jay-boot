package com.jaycong.boot.modules.rbac.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "用户角色分配请求")
public record UserAssignRolesRequest(
        @Schema(description = "角色 ID 列表", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "角色 ID 列表不能为空")
        List<@NotNull(message = "角色 ID 不能为空") @Positive(message = "角色 ID 必须为正数") Long> roleIds
) {
}

package com.jaycong.boot.modules.auth.dto;

import com.jaycong.boot.common.constant.enums.AdminUserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理端更新用户状态请求")
public record AdminUserStatusUpdateRequest(
        @Schema(description = "用户状态", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "状态不能为空")
        AdminUserStatus status
) {
}


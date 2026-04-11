package com.jaycong.boot.modules.plan.dto;

import com.jaycong.boot.common.constant.enums.PlanStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理端套餐状态更新请求")
public record AdminPlanStatusUpdateRequest(
        @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "状态不能为空")
        PlanStatus status
) {
}


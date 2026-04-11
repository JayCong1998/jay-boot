package com.jaycong.boot.modules.plan.dto;

import com.jaycong.boot.common.constant.enums.PlanBillingCycle;
import com.jaycong.boot.common.constant.enums.PlanStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理端套餐列表项")
public record AdminPlanItemView(
        @Schema(description = "套餐ID", example = "1001")
        Long id,
        @Schema(description = "套餐编码", example = "PRO_MONTHLY")
        String code,
        @Schema(description = "套餐名称", example = "专业版（月付）")
        String name,
        @Schema(description = "计费周期")
        PlanBillingCycle billingCycle,
        @Schema(description = "配额JSON", example = "{\"seats\":10}")
        String quotaJson,
        @Schema(description = "价格，单位分", example = "29900")
        Long price,
        @Schema(description = "状态")
        PlanStatus status,
        @Schema(description = "更新时间", example = "2026-04-10T16:30:00")
        String updatedTime
) {
}

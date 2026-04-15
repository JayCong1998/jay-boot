package com.jaycong.boot.modules.plan.dto;

import com.jaycong.boot.common.constant.enums.PlanBillingCycle;
import com.jaycong.boot.common.constant.enums.PlanStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理端套餐列表项")
public record AdminPlanItemView(
        @Schema(description = "套餐ID，序列化为字符串", example = "1949000000000001001", type = "string")
        Long id,
        @Schema(description = "套餐编码", example = "PRO_MONTHLY")
        String code,
        @Schema(description = "套餐名称", example = "Pro (Monthly)")
        String name,
        @Schema(description = "计费周期")
        PlanBillingCycle billingCycle,
        @Schema(description = "配额JSON", example = "{\"seats\":10}")
        String quotaJson,
        @Schema(description = "价格（分）", example = "29900")
        Long price,
        @Schema(description = "状态")
        PlanStatus status,
        @Schema(description = "最后更新时间", example = "2026-04-10T16:30:00")
        String updatedTime
) {
}

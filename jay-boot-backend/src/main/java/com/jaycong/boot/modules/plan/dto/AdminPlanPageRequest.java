package com.jaycong.boot.modules.plan.dto;

import com.jaycong.boot.common.constant.enums.PlanBillingCycle;
import com.jaycong.boot.common.constant.enums.PlanStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理端套餐分页请求")
public record AdminPlanPageRequest(
        @Schema(description = "页码", example = "1")
        Integer page,
        @Schema(description = "每页条数", example = "10")
        Integer pageSize,
        @Schema(description = "关键字，匹配套餐编码/名称", example = "PRO")
        String keyword,
        @Schema(description = "状态")
        PlanStatus status,
        @Schema(description = "计费周期")
        PlanBillingCycle billingCycle
) {
}


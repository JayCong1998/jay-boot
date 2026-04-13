package com.jaycong.boot.modules.plan.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.jaycong.boot.common.constant.enums.PlanBillingCycle;
import com.jaycong.boot.common.constant.enums.PlanStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Admin plan list item")
public record AdminPlanItemView(
        @Schema(description = "Plan ID serialized as string", example = "1949000000000001001", type = "string")
        @JsonSerialize(using = ToStringSerializer.class)
        Long id,
        @Schema(description = "Plan code", example = "PRO_MONTHLY")
        String code,
        @Schema(description = "Plan name", example = "Pro (Monthly)")
        String name,
        @Schema(description = "Billing cycle")
        PlanBillingCycle billingCycle,
        @Schema(description = "Quota JSON", example = "{\"seats\":10}")
        String quotaJson,
        @Schema(description = "Price in cents", example = "29900")
        Long price,
        @Schema(description = "Status")
        PlanStatus status,
        @Schema(description = "Last updated time", example = "2026-04-10T16:30:00")
        String updatedTime
) {
}

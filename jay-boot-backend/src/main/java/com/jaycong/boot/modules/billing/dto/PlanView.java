package com.jaycong.boot.modules.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 套餐响应视图。
 */
@Schema(description = "套餐信息")
public record PlanView(
        @Schema(description = "套餐 ID", example = "1001")
        Long planId,
        @Schema(description = "套餐编码", example = "PRO")
        String code,
        @Schema(description = "套餐名称", example = "专业版")
        String name,
        @Schema(description = "计费周期", example = "MONTHLY")
        String billingCycle,
        @Schema(description = "额度配置 JSON", example = "{\"users\":10,\"storageGb\":100}")
        String quotaJson,
        @Schema(description = "价格（最小货币单位）", example = "19900")
        Long price,
        @Schema(description = "套餐状态", example = "ACTIVE")
        String status
) {
}

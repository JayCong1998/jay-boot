package com.jaycong.boot.modules.plan.dto;

import com.jaycong.boot.common.constant.enums.PlanBillingCycle;
import com.jaycong.boot.common.constant.enums.PlanStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "管理端创建套餐请求")
public record AdminPlanCreateRequest(
        @Schema(description = "套餐编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "PRO_MONTHLY")
        @NotBlank(message = "套餐编码不能为空")
        @Size(max = 64, message = "套餐编码长度不能超过64")
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "套餐编码仅支持大写字母、数字和下划线")
        String code,
        @Schema(description = "套餐名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "专业版（月付）")
        @NotBlank(message = "套餐名称不能为空")
        @Size(max = 64, message = "套餐名称长度不能超过64")
        String name,
        @Schema(description = "计费周期", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "计费周期不能为空")
        PlanBillingCycle billingCycle,
        @Schema(description = "配额JSON", requiredMode = Schema.RequiredMode.REQUIRED, example = "{\"seats\":10}")
        @NotBlank(message = "配额JSON不能为空")
        String quotaJson,
        @Schema(description = "价格，单位分", requiredMode = Schema.RequiredMode.REQUIRED, example = "29900")
        @NotNull(message = "价格不能为空")
        @Min(value = 0, message = "价格不能小于0")
        Long price,
        @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "状态不能为空")
        PlanStatus status
) {
}


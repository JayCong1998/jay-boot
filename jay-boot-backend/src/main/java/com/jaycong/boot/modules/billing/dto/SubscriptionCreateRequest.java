package com.jaycong.boot.modules.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

/**
 * 创建订阅请求。
 */
@Schema(description = "创建订阅请求")
public record SubscriptionCreateRequest(
        @Schema(description = "套餐 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
        @NotNull(message = "套餐 ID 不能为空")
        @Positive(message = "套餐 ID 必须为正数")
        Long planId,
        @Schema(description = "初始状态，仅支持 TRIALING 或 ACTIVE，默认 TRIALING", example = "TRIALING")
        String initialStatus,
        @Schema(description = "试用结束时间", example = "2026-04-30T23:59:59")
        LocalDateTime trialEndAt,
        @Schema(description = "当前计费周期结束时间", example = "2026-05-01T00:00:00")
        LocalDateTime currentPeriodEnd
) {
}

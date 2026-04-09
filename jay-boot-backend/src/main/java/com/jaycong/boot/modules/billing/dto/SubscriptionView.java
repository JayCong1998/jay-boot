package com.jaycong.boot.modules.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * 订阅响应视图。
 */
@Schema(description = "订阅信息")
public record SubscriptionView(
        @Schema(description = "订阅 ID", example = "2001")
        Long subscriptionId,
        @Schema(description = "租户 ID", example = "3001")
        Long tenantId,
        @Schema(description = "套餐 ID", example = "1001")
        Long planId,
        @Schema(description = "订阅状态", example = "TRIALING")
        String status,
        @Schema(description = "试用结束时间", example = "2026-04-30T23:59:59")
        LocalDateTime trialEndAt,
        @Schema(description = "当前计费周期结束时间", example = "2026-05-01T00:00:00")
        LocalDateTime currentPeriodEnd,
        @Schema(description = "生效时间", example = "2026-04-01T12:00:00")
        LocalDateTime effectiveTime,
        @Schema(description = "取消时间", example = "2026-05-15T12:00:00")
        LocalDateTime cancelTime
) {
}

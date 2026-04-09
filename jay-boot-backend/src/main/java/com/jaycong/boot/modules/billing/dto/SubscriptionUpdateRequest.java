package com.jaycong.boot.modules.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 更新订阅请求。
 */
@Schema(description = "更新订阅请求")
public record SubscriptionUpdateRequest(
        @Schema(description = "目标状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "ACTIVE")
        @NotBlank(message = "目标状态不能为空")
        String targetStatus,
        @Schema(description = "当前计费周期结束时间", example = "2026-06-01T00:00:00")
        LocalDateTime currentPeriodEnd,
        @Schema(description = "取消时间（目标状态为 CANCELED 时可传）", example = "2026-05-15T12:00:00")
        LocalDateTime cancelTime
) {
}

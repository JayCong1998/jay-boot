package com.jaycong.boot.modules.mail.dto;

import com.jaycong.boot.modules.mail.constant.MailStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理端更新邮件通道状态请求")
public record AdminMailChannelStatusUpdateRequest(
        @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "状态不能为空")
        MailStatus status
) {
}


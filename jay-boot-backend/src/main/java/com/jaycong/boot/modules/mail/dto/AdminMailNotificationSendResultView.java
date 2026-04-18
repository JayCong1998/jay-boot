package com.jaycong.boot.modules.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "管理端系统通知发送结果")
public record AdminMailNotificationSendResultView(
        @Schema(description = "发送成功数量")
        int successCount,
        @Schema(description = "发送失败数量")
        int failedCount,
        @Schema(description = "发送失败邮箱列表")
        List<String> failedEmails
) {
}


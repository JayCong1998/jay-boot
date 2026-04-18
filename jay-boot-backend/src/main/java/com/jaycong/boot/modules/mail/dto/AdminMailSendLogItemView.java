package com.jaycong.boot.modules.mail.dto;

import com.jaycong.boot.modules.mail.constant.MailBizType;
import com.jaycong.boot.modules.mail.constant.MailSendStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理端邮件发送日志项")
public record AdminMailSendLogItemView(
        @Schema(description = "日志ID", type = "string")
        Long id,
        @Schema(description = "业务类型")
        MailBizType bizType,
        @Schema(description = "场景编码")
        String sceneCode,
        @Schema(description = "模板编码")
        String templateCode,
        @Schema(description = "通道编码")
        String channelCode,
        @Schema(description = "收件邮箱")
        String recipientEmail,
        @Schema(description = "渲染主题")
        String subjectRendered,
        @Schema(description = "渲染正文")
        String bodyRendered,
        @Schema(description = "业务幂等键")
        String bizKey,
        @Schema(description = "链路追踪ID")
        String traceId,
        @Schema(description = "状态")
        MailSendStatus status,
        @Schema(description = "错误码")
        String errorCode,
        @Schema(description = "错误信息")
        String errorMessage,
        @Schema(description = "重试次数")
        Integer retryCount,
        @Schema(description = "最大重试次数")
        Integer maxRetryCount,
        @Schema(description = "下次重试时间")
        String nextRetryTime,
        @Schema(description = "发送时间")
        String sentTime,
        @Schema(description = "创建时间")
        String createdTime
) {
}


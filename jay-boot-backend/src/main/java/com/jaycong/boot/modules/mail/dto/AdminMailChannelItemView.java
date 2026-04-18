package com.jaycong.boot.modules.mail.dto;

import com.jaycong.boot.modules.mail.constant.MailStatus;
import com.jaycong.boot.modules.mail.constant.MailTlsMode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理端邮件通道列表项")
public record AdminMailChannelItemView(
        @Schema(description = "通道ID", type = "string", example = "1952000000000000001")
        Long id,
        @Schema(description = "通道编码")
        String channelCode,
        @Schema(description = "通道名称")
        String channelName,
        @Schema(description = "SMTP主机")
        String smtpHost,
        @Schema(description = "SMTP端口")
        Integer smtpPort,
        @Schema(description = "SMTP用户名")
        String smtpUsername,
        @Schema(description = "SMTP密码（脱敏）")
        String smtpPasswordMasked,
        @Schema(description = "TLS模式")
        MailTlsMode tlsMode,
        @Schema(description = "发件人名称")
        String fromName,
        @Schema(description = "发件人邮箱")
        String fromEmail,
        @Schema(description = "优先级")
        Integer priority,
        @Schema(description = "状态")
        MailStatus status,
        @Schema(description = "备注")
        String remark,
        @Schema(description = "更新时间")
        String updatedTime
) {
}


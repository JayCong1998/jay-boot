package com.jaycong.boot.modules.mail.dto;

import com.jaycong.boot.modules.mail.constant.MailStatus;
import com.jaycong.boot.modules.mail.constant.MailTlsMode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "管理端创建邮件通道请求")
public record AdminMailChannelCreateRequest(
        @Schema(description = "通道编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "default_smtp")
        @NotBlank(message = "通道编码不能为空")
        @Size(max = 64, message = "通道编码长度不能超过64")
        String channelCode,
        @Schema(description = "通道名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "默认SMTP")
        @NotBlank(message = "通道名称不能为空")
        @Size(max = 64, message = "通道名称长度不能超过64")
        String channelName,
        @Schema(description = "SMTP主机", requiredMode = Schema.RequiredMode.REQUIRED, example = "smtp.example.com")
        @NotBlank(message = "SMTP主机不能为空")
        @Size(max = 128, message = "SMTP主机长度不能超过128")
        String smtpHost,
        @Schema(description = "SMTP端口", requiredMode = Schema.RequiredMode.REQUIRED, example = "587")
        @NotNull(message = "SMTP端口不能为空")
        @Min(value = 1, message = "SMTP端口必须大于0")
        @Max(value = 65535, message = "SMTP端口不能超过65535")
        Integer smtpPort,
        @Schema(description = "SMTP用户名", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "SMTP用户名不能为空")
        @Size(max = 128, message = "SMTP用户名长度不能超过128")
        String smtpUsername,
        @Schema(description = "SMTP密码", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "SMTP密码不能为空")
        @Size(max = 256, message = "SMTP密码长度不能超过256")
        String smtpPassword,
        @Schema(description = "TLS模式", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "TLS模式不能为空")
        MailTlsMode tlsMode,
        @Schema(description = "发件人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "Jay Boot")
        @NotBlank(message = "发件人名称不能为空")
        @Size(max = 128, message = "发件人名称长度不能超过128")
        String fromName,
        @Schema(description = "发件人邮箱", requiredMode = Schema.RequiredMode.REQUIRED, example = "no-reply@example.com")
        @NotBlank(message = "发件人邮箱不能为空")
        @Email(message = "发件人邮箱格式不正确")
        @Size(max = 128, message = "发件人邮箱长度不能超过128")
        String fromEmail,
        @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
        @NotNull(message = "优先级不能为空")
        Integer priority,
        @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "状态不能为空")
        MailStatus status,
        @Schema(description = "备注")
        @Size(max = 255, message = "备注长度不能超过255")
        String remark
) {
}


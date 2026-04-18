package com.jaycong.boot.modules.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "邮件模板预览结果")
public record MailTemplatePreviewView(
        @Schema(description = "渲染主题")
        String subject,
        @Schema(description = "渲染正文")
        String body
) {
}


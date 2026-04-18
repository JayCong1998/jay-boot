package com.jaycong.boot.modules.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

@Schema(description = "邮件模板预览请求")
public record MailTemplatePreviewRequest(
        @Schema(description = "模板编码", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "模板编码不能为空")
        String templateCode,
        @Schema(description = "变量")
        Map<String, Object> variables
) {
}


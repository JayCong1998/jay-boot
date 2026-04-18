package com.jaycong.boot.modules.mail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@Schema(description = "管理端系统通知发送请求")
public record AdminMailNotificationSendRequest(
        @Schema(description = "模板编码", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "模板编码不能为空")
        String templateCode,
        @Schema(description = "收件人邮箱列表", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "收件人不能为空")
        @Size(max = 200, message = "单次最多发送200个收件人")
        List<String> recipientEmails,
        @Schema(description = "模板变量")
        Map<String, Object> variables,
        @Schema(description = "业务幂等键")
        @Size(max = 128, message = "业务幂等键长度不能超过128")
        String bizKey
) {
}


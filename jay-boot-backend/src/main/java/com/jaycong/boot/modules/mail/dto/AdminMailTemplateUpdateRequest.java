package com.jaycong.boot.modules.mail.dto;

import com.jaycong.boot.modules.mail.constant.MailBizType;
import com.jaycong.boot.modules.mail.constant.MailBodyType;
import com.jaycong.boot.modules.mail.constant.MailStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "管理端更新邮件模板请求")
public record AdminMailTemplateUpdateRequest(
        @Schema(description = "模板名称", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "模板名称不能为空")
        @Size(max = 64, message = "模板名称长度不能超过64")
        String templateName,
        @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "业务类型不能为空")
        MailBizType bizType,
        @Schema(description = "场景编码", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "场景编码不能为空")
        @Size(max = 64, message = "场景编码长度不能超过64")
        String sceneCode,
        @Schema(description = "主题模板", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "主题模板不能为空")
        @Size(max = 255, message = "主题模板长度不能超过255")
        String subjectTemplate,
        @Schema(description = "正文模板", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "正文模板不能为空")
        String bodyTemplate,
        @Schema(description = "正文类型", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "正文类型不能为空")
        MailBodyType bodyType,
        @Schema(description = "变量Schema JSON")
        String varsSchemaJson,
        @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "状态不能为空")
        MailStatus status,
        @Schema(description = "备注")
        @Size(max = 255, message = "备注长度不能超过255")
        String remark
) {
}


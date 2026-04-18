package com.jaycong.boot.modules.mail.dto;

import com.jaycong.boot.modules.mail.constant.MailBizType;
import com.jaycong.boot.modules.mail.constant.MailBodyType;
import com.jaycong.boot.modules.mail.constant.MailStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理端邮件模板列表项")
public record AdminMailTemplateItemView(
        @Schema(description = "模板ID", type = "string", example = "1952000000000000010")
        Long id,
        @Schema(description = "模板编码")
        String templateCode,
        @Schema(description = "模板名称")
        String templateName,
        @Schema(description = "业务类型")
        MailBizType bizType,
        @Schema(description = "场景编码")
        String sceneCode,
        @Schema(description = "主题模板")
        String subjectTemplate,
        @Schema(description = "正文模板")
        String bodyTemplate,
        @Schema(description = "正文类型")
        MailBodyType bodyType,
        @Schema(description = "变量Schema JSON")
        String varsSchemaJson,
        @Schema(description = "状态")
        MailStatus status,
        @Schema(description = "备注")
        String remark,
        @Schema(description = "更新时间")
        String updatedTime
) {
}


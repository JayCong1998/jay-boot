package com.jaycong.boot.modules.mail.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jaycong.boot.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mail_template")
public class MailTemplateEntity extends BaseEntity {

    private String templateCode;
    private String templateName;
    private String bizType;
    private String sceneCode;
    private String subjectTemplate;
    private String bodyTemplate;
    private String bodyType;
    private String varsSchemaJson;
    private String status;
    private String remark;
}


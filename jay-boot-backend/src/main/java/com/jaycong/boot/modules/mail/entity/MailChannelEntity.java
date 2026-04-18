package com.jaycong.boot.modules.mail.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jaycong.boot.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mail_channel")
public class MailChannelEntity extends BaseEntity {

    private String channelCode;
    private String channelName;
    private String providerType;
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPasswordCipher;
    private String tlsMode;
    private String fromName;
    private String fromEmail;
    private Integer priority;
    private String status;
    private String remark;
}


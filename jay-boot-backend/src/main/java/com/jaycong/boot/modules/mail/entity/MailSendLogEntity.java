package com.jaycong.boot.modules.mail.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jaycong.boot.common.model.BaseEntity;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mail_send_log")
public class MailSendLogEntity extends BaseEntity {

    private String bizType;
    private String sceneCode;
    private String templateCode;
    private String channelCode;
    private String recipientEmail;
    private String subjectRendered;
    private String bodyRendered;
    private String bizKey;
    private String traceId;
    private String status;
    private String errorCode;
    private String errorMessage;
    private Integer retryCount;
    private Integer maxRetryCount;
    private LocalDateTime nextRetryTime;
    private LocalDateTime sentTime;
}


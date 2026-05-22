package com.jaycong.boot.capability.mail.dto;

import com.jaycong.boot.capability.mail.constant.MailBizType;
import com.jaycong.boot.capability.mail.constant.MailSendStatus;

public record AdminMailSendLogPageRequest(
        Integer page,
        Integer pageSize,
        MailBizType bizType,
        String sceneCode,
        String templateCode,
        String recipientEmail,
        MailSendStatus status,
        String startTime,
        String endTime
) {
}


package com.jaycong.boot.modules.mail.dto;

import com.jaycong.boot.modules.mail.constant.MailBizType;
import com.jaycong.boot.modules.mail.constant.MailSendStatus;

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


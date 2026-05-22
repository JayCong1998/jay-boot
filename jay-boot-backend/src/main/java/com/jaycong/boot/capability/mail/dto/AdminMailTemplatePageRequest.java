package com.jaycong.boot.capability.mail.dto;

import com.jaycong.boot.capability.mail.constant.MailBizType;
import com.jaycong.boot.capability.mail.constant.MailStatus;

public record AdminMailTemplatePageRequest(
        Integer page,
        Integer pageSize,
        String keyword,
        MailBizType bizType,
        String sceneCode,
        MailStatus status
) {
}


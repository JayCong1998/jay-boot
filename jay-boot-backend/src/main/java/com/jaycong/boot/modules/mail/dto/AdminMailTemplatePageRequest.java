package com.jaycong.boot.modules.mail.dto;

import com.jaycong.boot.modules.mail.constant.MailBizType;
import com.jaycong.boot.modules.mail.constant.MailStatus;

public record AdminMailTemplatePageRequest(
        Integer page,
        Integer pageSize,
        String keyword,
        MailBizType bizType,
        String sceneCode,
        MailStatus status
) {
}


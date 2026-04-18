package com.jaycong.boot.modules.mail.dto;

import com.jaycong.boot.modules.mail.constant.MailStatus;

public record AdminMailChannelPageRequest(
        Integer page,
        Integer pageSize,
        String keyword,
        MailStatus status
) {
}


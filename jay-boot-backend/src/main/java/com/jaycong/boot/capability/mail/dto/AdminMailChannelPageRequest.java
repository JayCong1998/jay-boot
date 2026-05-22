package com.jaycong.boot.capability.mail.dto;

import com.jaycong.boot.capability.mail.constant.MailStatus;

public record AdminMailChannelPageRequest(
        Integer page,
        Integer pageSize,
        String keyword,
        MailStatus status
) {
}


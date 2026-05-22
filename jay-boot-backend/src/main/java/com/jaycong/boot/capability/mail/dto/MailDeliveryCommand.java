package com.jaycong.boot.capability.mail.dto;

import com.jaycong.boot.capability.mail.constant.MailBizType;
import java.util.Map;

public record MailDeliveryCommand(
        MailBizType bizType,
        String sceneCode,
        String templateCode,
        String recipientEmail,
        Map<String, Object> variables,
        String bizKey,
        String traceId,
        boolean strictMode
) {
}


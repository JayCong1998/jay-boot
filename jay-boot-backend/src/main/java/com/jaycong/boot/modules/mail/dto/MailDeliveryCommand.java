package com.jaycong.boot.modules.mail.dto;

import com.jaycong.boot.modules.mail.constant.MailBizType;
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


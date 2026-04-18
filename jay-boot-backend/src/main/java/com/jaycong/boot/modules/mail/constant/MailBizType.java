package com.jaycong.boot.modules.mail.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum MailBizType {
    VERIFY_CODE("VERIFY_CODE", "验证码"),
    SYSTEM_NOTICE("SYSTEM_NOTICE", "系统通知");

    private final String value;
    private final String label;

    MailBizType(String value, String label) {
        this.value = value;
        this.label = label;
    }

    @JsonValue
    public String value() {
        return value;
    }

    public String label() {
        return label;
    }

    @JsonCreator
    public static MailBizType fromValue(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim();
        return Arrays.stream(values())
                .filter(item -> item.value.equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported mail biz type: " + raw));
    }
}


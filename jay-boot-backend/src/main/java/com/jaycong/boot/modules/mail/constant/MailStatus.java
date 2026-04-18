package com.jaycong.boot.modules.mail.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum MailStatus {
    ENABLED("ENABLED", "启用"),
    DISABLED("DISABLED", "禁用");

    private final String value;
    private final String label;

    MailStatus(String value, String label) {
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
    public static MailStatus fromValue(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim();
        return Arrays.stream(values())
                .filter(item -> item.value.equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported mail status: " + raw));
    }
}


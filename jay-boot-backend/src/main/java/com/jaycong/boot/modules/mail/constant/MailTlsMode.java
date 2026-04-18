package com.jaycong.boot.modules.mail.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum MailTlsMode {
    NONE("NONE", "无加密"),
    STARTTLS("STARTTLS", "STARTTLS"),
    SSL("SSL", "SSL");

    private final String value;
    private final String label;

    MailTlsMode(String value, String label) {
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
    public static MailTlsMode fromValue(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim();
        return Arrays.stream(values())
                .filter(item -> item.value.equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported mail tls mode: " + raw));
    }
}


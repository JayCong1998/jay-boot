package com.jaycong.boot.common.constant.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum AdminUserStatus {
    ACTIVE("ACTIVE", "激活"),
    INACTIVE("INACTIVE", "停用");

    private final String value;
    private final String label;

    AdminUserStatus(String value, String label) {
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

    public static AdminUserStatus fromValue(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim();
        return Arrays.stream(values())
                .filter(item -> item.value.equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported admin user status: " + raw));
    }

    @JsonCreator
    public static AdminUserStatus fromRaw(String raw) {
        return fromValue(raw);
    }
}


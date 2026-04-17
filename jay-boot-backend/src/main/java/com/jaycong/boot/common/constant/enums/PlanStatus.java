package com.jaycong.boot.common.constant.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum PlanStatus {
    ACTIVE("ACTIVE", "启用"),
    INACTIVE("INACTIVE", "禁用");

    private final String value;
    private final String label;

    PlanStatus(String value, String label) {
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

    public static PlanStatus fromValue(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim();
        return Arrays.stream(values())
                .filter(item -> item.value.equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported plan status: " + raw));
    }

    @JsonCreator
    public static PlanStatus fromRaw(String raw) {
        return fromValue(raw);
    }
}


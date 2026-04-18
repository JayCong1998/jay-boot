package com.jaycong.boot.modules.mail.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum MailSceneCode {
    REGISTER("REGISTER", "注册"),
    LOGIN_VERIFY("LOGIN_VERIFY", "登录验证"),
    RESET_PASSWORD("RESET_PASSWORD", "找回密码"),
    CHANGE_EMAIL("CHANGE_EMAIL", "修改邮箱");

    private final String value;
    private final String label;

    MailSceneCode(String value, String label) {
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
    public static MailSceneCode fromValue(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim();
        return Arrays.stream(values())
                .filter(item -> item.value.equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported mail scene code: " + raw));
    }
}


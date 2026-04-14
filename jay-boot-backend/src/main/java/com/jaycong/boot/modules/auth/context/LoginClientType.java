package com.jaycong.boot.modules.auth.context;

public enum LoginClientType {
    ADMIN,
    SITE;

    public static LoginClientType resolve(String value) {
        if (value == null) {
            return ADMIN;
        }
        for (LoginClientType clientType : values()) {
            if (clientType.name().equalsIgnoreCase(value.trim())) {
                return clientType;
            }
        }
        return ADMIN;
    }
}

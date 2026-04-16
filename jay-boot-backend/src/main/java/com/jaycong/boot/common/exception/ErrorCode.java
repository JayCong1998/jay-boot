package com.jaycong.boot.common.exception;

public enum ErrorCode {
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    CONFLICT(409),
    TOO_MANY_REQUESTS(429),
    INTERNAL_ERROR(500);

    private final int code;

    ErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

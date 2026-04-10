package com.jaycong.boot.common.web;

public record ApiResponse<T>(int code, T body, String message, boolean success) {

    public static <T> ApiResponse<T> success(T body) {
        return new ApiResponse<>(200, body, "OK", true);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, null, message, false);
    }
}

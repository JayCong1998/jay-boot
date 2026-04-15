package com.jaycong.boot.modules.log.context;

/**
 * 请求上下文，用于存储当前请求的相关信息
 */
public final class RequestContext {

    private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();

    private final String requestId;
    private final long startTimeMillis;
    private final Long userId;
    private final String username;

    private RequestContext(String requestId, long startTimeMillis, Long userId, String username) {
        this.requestId = requestId;
        this.startTimeMillis = startTimeMillis;
        this.userId = userId;
        this.username = username;
    }

    public static void init(String requestId, Long userId, String username) {
        RequestContext context = new RequestContext(requestId, System.currentTimeMillis(), userId, username);
        CONTEXT.set(context);
    }

    public static RequestContext current() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public String getRequestId() {
        return requestId;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public long getDurationMillis() {
        return System.currentTimeMillis() - startTimeMillis;
    }
}

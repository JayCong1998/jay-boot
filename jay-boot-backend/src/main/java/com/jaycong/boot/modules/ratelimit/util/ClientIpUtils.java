package com.jaycong.boot.modules.ratelimit.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 客户端 IP 获取工具
 */
public final class ClientIpUtils {
    
    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    
    private ClientIpUtils() {
    }
    
    /**
     * 获取客户端真实 IP 地址
     * 优先级：X-Forwarded-For > X-Real-IP > RemoteAddr
     *
     * @param request HTTP 请求
     * @return 客户端 IP
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // X-Forwarded-For 可能包含多个 IP，取第一个
            int index = ip.indexOf(',');
            if (index != -1) {
                ip = ip.substring(0, index).trim();
            }
            return ip;
        }
        
        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        ip = request.getRemoteAddr();
        if (ip == null || ip.isEmpty()) {
            return UNKNOWN;
        }
        
        // IPv6 本地地址转换为 IPv4
        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IPV4;
        }
        
        return ip;
    }
    
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip);
    }
}

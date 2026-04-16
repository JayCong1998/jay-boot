package com.jaycong.boot.modules.dashboard.dto;

/**
 * 仪表盘事件项视图
 */
public record DashboardEventItemView(
        String id,
        String time,
        String module,
        String event,
        String traceId,
        String status
) {}

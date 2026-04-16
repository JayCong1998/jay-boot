package com.jaycong.boot.modules.dashboard.dto;

/**
 * 仪表盘告警项视图
 */
public record DashboardAlertItemView(
        String id,
        String module,
        String level,
        String message,
        String time
) {}

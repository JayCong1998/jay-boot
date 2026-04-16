package com.jaycong.boot.modules.dashboard.dto;

/**
 * 仪表盘 KPI 指标项视图
 */
public record DashboardKpiItemView(
        String key,
        String title,
        String valueText,
        String trendText,
        String trendDirection
) {}

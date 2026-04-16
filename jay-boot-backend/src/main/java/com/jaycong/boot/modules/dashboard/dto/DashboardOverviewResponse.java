package com.jaycong.boot.modules.dashboard.dto;

import java.util.List;

/**
 * 仪表盘总览响应
 */
public record DashboardOverviewResponse(
        String range,
        String updatedAt,
        List<DashboardKpiItemView> kpis,
        List<DashboardAlertItemView> alerts,
        List<DashboardEventItemView> events
) {}

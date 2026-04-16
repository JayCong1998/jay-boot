package com.jaycong.boot.rest.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.modules.dashboard.dto.DashboardAlertItemView;
import com.jaycong.boot.modules.dashboard.dto.DashboardEventItemView;
import com.jaycong.boot.modules.dashboard.dto.DashboardKpiItemView;
import com.jaycong.boot.modules.dashboard.dto.DashboardOverviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 管理端仪表盘控制器
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@Validated
@Tag(name = "管理端仪表盘", description = "仪表盘总览数据")
@SaCheckRole(value = {"admin", "super_admin"}, mode = SaMode.OR)
public class AdminDashboardController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Operation(summary = "获取仪表盘总览数据")
    @GetMapping("/overview")
    public ApiResponse<DashboardOverviewResponse> overview(
            @RequestParam(value = "range", required = false, defaultValue = "7d") String range) {
        DashboardOverviewResponse response = buildMockData(range);
        return ApiResponse.success(response);
    }

    private DashboardOverviewResponse buildMockData(String range) {
        String updatedAt = LocalDateTime.now().format(TIME_FORMATTER);

        List<DashboardKpiItemView> kpis = List.of(
                new DashboardKpiItemView("active_users", "活跃用户", "1,234", "↑ 12.5%", "up"),
                new DashboardKpiItemView("api_calls", "API 调用次数", "56,789", "↑ 8.3%", "up"),
                new DashboardKpiItemView("new_tenants", "新增租户", "45", "↓ 3.2%", "down"),
                new DashboardKpiItemView("revenue", "本月收入", "¥ 128,500", "→ 持平", "flat")
        );

        List<DashboardAlertItemView> alerts = List.of(
                new DashboardAlertItemView("alert-001", "API Gateway", "warn", "API 响应时间超过阈值 ( > 500ms )", "10:23:45"),
                new DashboardAlertItemView("alert-002", "计费系统", "error", "租户 tenant-008 套餐额度即将耗尽", "09:15:22"),
                new DashboardAlertItemView("alert-003", "用户系统", "info", "今日新增用户数达到 100 人", "08:30:00")
        );

        List<DashboardEventItemView> events = List.of(
                new DashboardEventItemView("evt-001", "10:45:12", "API Gateway", "请求处理完成", "trace-a1b2c3d4", "success"),
                new DashboardEventItemView("evt-002", "10:44:58", "计费系统", "套餐续费成功", "trace-e5f6g7h8", "success"),
                new DashboardEventItemView("evt-003", "10:43:30", "用户系统", "用户登录失败重试", "trace-i9j0k1l2", "retrying"),
                new DashboardEventItemView("evt-004", "10:42:15", "AI Gateway", "模型调用超时", "trace-m3n4o5p6", "failed"),
                new DashboardEventItemView("evt-005", "10:40:22", "租户系统", "租户创建成功", "trace-q7r8s9t0", "success")
        );

        return new DashboardOverviewResponse(range, updatedAt, kpis, alerts, events);
    }
}

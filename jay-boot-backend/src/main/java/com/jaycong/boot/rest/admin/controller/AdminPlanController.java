package com.jaycong.boot.rest.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.jaycong.boot.common.constant.enums.PlanBillingCycle;
import com.jaycong.boot.common.constant.enums.PlanStatus;
import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.plan.dto.AdminPlanCreateRequest;
import com.jaycong.boot.modules.plan.dto.AdminPlanItemView;
import com.jaycong.boot.modules.plan.dto.AdminPlanPageRequest;
import com.jaycong.boot.modules.plan.dto.AdminPlanStatusUpdateRequest;
import com.jaycong.boot.modules.plan.dto.AdminPlanUpdateRequest;
import com.jaycong.boot.modules.plan.service.AdminPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/plans")
@Validated
@Tag(name = "管理端套餐管理", description = "套餐分页、创建、编辑与状态启停")
@SaCheckRole(value = {"admin", "super_admin"}, mode = SaMode.OR)
public class AdminPlanController {

    private final AdminPlanService adminPlanService;

    public AdminPlanController(AdminPlanService adminPlanService) {
        this.adminPlanService = adminPlanService;
    }

    @Operation(summary = "分页查询套餐")
    @GetMapping("/page")
    public ApiResponse<PageResult<AdminPlanItemView>> page(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) PlanStatus status,
            @RequestParam(required = false) PlanBillingCycle billingCycle) {
        return ApiResponse.success(adminPlanService.page(new AdminPlanPageRequest(page, pageSize, keyword, status, billingCycle)));
    }

    @Operation(summary = "创建套餐")
    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody AdminPlanCreateRequest request) {
        adminPlanService.create(request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "更新套餐")
    @PostMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody AdminPlanUpdateRequest request) {
        adminPlanService.update(id, request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "更新套餐状态")
    @PostMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody AdminPlanStatusUpdateRequest request) {
        adminPlanService.updateStatus(id, request);
        return ApiResponse.success(null);
    }
}


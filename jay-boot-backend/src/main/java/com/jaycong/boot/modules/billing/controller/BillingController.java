package com.jaycong.boot.modules.billing.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.modules.billing.dto.PlanView;
import com.jaycong.boot.modules.billing.dto.SubscriptionCreateRequest;
import com.jaycong.boot.modules.billing.dto.SubscriptionUpdateRequest;
import com.jaycong.boot.modules.billing.dto.SubscriptionView;
import com.jaycong.boot.modules.billing.service.BillingService;
import com.jaycong.boot.modules.rbac.service.RbacPermissionCatalog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Billing 控制器，提供套餐查询与订阅管理接口。
 */
@RestController
@RequestMapping("/api/billing")
@Validated
@Tag(name = "Billing 管理", description = "套餐与订阅管理")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    /**
     * 查询可用套餐列表。
     */
    @Operation(summary = "查询套餐列表")
    @SaCheckPermission(RbacPermissionCatalog.BILLING_PLAN_READ)
    @GetMapping("/plans")
    public ApiResponse<List<PlanView>> plans() {
        return ApiResponse.success(billingService.listPlans());
    }

    /**
     * 为当前租户创建订阅。
     */
    @Operation(summary = "创建当前租户订阅")
    @SaCheckPermission(RbacPermissionCatalog.BILLING_SUBSCRIPTION_CREATE)
    @PostMapping("/subscriptions")
    public ApiResponse<SubscriptionView> createSubscription(@Valid @RequestBody SubscriptionCreateRequest request) {
        return ApiResponse.success(billingService.createSubscription(request));
    }

    /**
     * 更新指定订阅状态。
     */
    @Operation(summary = "更新订阅状态")
    @SaCheckPermission(RbacPermissionCatalog.BILLING_SUBSCRIPTION_UPDATE)
    @PostMapping("/subscriptions/{id}/update")
    public ApiResponse<SubscriptionView> updateSubscription(
            @PathVariable("id") @Positive(message = "订阅 ID 必须为正数") Long id,
            @Valid @RequestBody SubscriptionUpdateRequest request) {
        return ApiResponse.success(billingService.updateSubscription(id, request));
    }
}

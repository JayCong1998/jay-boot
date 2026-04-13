package com.jaycong.boot.rest.site.controller;

import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.modules.plan.dto.SitePricingOverviewResponse;
import com.jaycong.boot.modules.plan.service.SitePricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端定价页控制器。
 */
@RestController
@RequestMapping("/api/user/pricing")
@Validated
@Tag(name = "用户端定价", description = "订阅定价页数据接口")
public class PricingController {

    private final SitePricingService sitePricingService;

    public PricingController(SitePricingService sitePricingService) {
        this.sitePricingService = sitePricingService;
    }

    /**
     * 获取定价页概览数据。
     */
    @Operation(summary = "获取定价页概览")
    @GetMapping("/overview")
    public ApiResponse<SitePricingOverviewResponse> overview(
            @Parameter(description = "计费周期（MONTHLY/YEARLY）")
            @RequestParam(value = "billingCycle", required = false) String billingCycle) {
        return new ApiResponse<>(200, sitePricingService.getOverview(billingCycle), "获取定价页数据成功", true);
    }
}
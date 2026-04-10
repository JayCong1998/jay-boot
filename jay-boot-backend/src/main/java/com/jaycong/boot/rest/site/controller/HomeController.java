package com.jaycong.boot.rest.site.controller;

import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.modules.home.dto.HomeCaseItemView;
import com.jaycong.boot.modules.home.dto.HomeOverviewResponse;
import com.jaycong.boot.modules.home.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端首页控制器，提供首页概览与案例列表接口。
 */
@RestController
@RequestMapping("/api/user/home")
@Validated
@Tag(name = "用户端首页", description = "首页概览与案例列表")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    /**
     * 获取首页概览数据。
     */
    @Operation(summary = "获取首页概览数据")
    @GetMapping("/overview")
    public ApiResponse<HomeOverviewResponse> overview() {
        return new ApiResponse<>(200, homeService.getOverview(), "获取首页数据成功", true);
    }

    /**
     * 获取首页案例列表。
     */
    @Operation(summary = "获取首页案例列表")
    @GetMapping("/cases")
    public ApiResponse<List<HomeCaseItemView>> cases(@RequestParam(value = "limit", required = false) Integer limit) {
        return new ApiResponse<>(200, homeService.getCases(limit), "获取案例列表成功", true);
    }
}


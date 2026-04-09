package com.jaycong.boot.modules.tenant.controller;

import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.modules.tenant.dto.TenantCurrentResponse;
import com.jaycong.boot.modules.tenant.dto.TenantUpdateCurrentRequest;
import com.jaycong.boot.modules.tenant.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 租户控制器，提供当前租户查询与更新接口。
 */
@RestController
@RequestMapping("/api/tenants")
@Validated
@Tag(name = "租户管理", description = "当前租户查询与更新")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    /**
     * 查询当前登录用户所属租户信息。
     */
    @Operation(summary = "查询当前租户")
    @GetMapping("/current")
    public ApiResponse<TenantCurrentResponse> current() {
        return ApiResponse.success(tenantService.getCurrentTenant());
    }

    /**
     * 更新当前登录用户所属租户名称。
     */
    @Operation(summary = "更新当前租户")
    @PostMapping("/current/update")
    public ApiResponse<TenantCurrentResponse> updateCurrent(@Valid @RequestBody TenantUpdateCurrentRequest request) {
        return ApiResponse.success(tenantService.updateCurrentTenant(request));
    }
}

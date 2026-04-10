package com.jaycong.boot.rest.admin.controller;

import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.modules.auth.dto.*;
import com.jaycong.boot.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器，提供注册、登录、会话和密码管理接口。
 */
@RestController
@RequestMapping("/api/admin/auth")
@Validated
@Tag(name = "认证管理", description = "用户注册、登录、会话与密码管理")
public class AdminAuthController {

    private final AuthService authService;

    public AdminAuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户注册并返回登录态信息。
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResponse<AuthTokenResponse> register(@Valid @RequestBody RegisterRequest request,
                                                   HttpServletRequest httpRequest) {
        return ApiResponse.success(authService.register(request, buildContext(httpRequest)));
    }

    /**
     * 用户登录并返回登录态信息。
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request,
                                                HttpServletRequest httpRequest) {
        return ApiResponse.success(authService.login(request, buildContext(httpRequest)));
    }

    /**
     * 当前登录用户退出登录。
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success(null);
    }

    /**
     * 获取当前登录会话信息。
     */
    @Operation(summary = "获取当前会话")
    @GetMapping("/session")
    public ApiResponse<AuthSessionResponse> session() {
        return ApiResponse.success(authService.session());
    }

    /**
     * 刷新当前登录令牌。
     */
    @Operation(summary = "刷新令牌")
    @PostMapping("/token/refresh")
    public ApiResponse<AuthTokenResponse> refreshToken() {
        return ApiResponse.success(authService.refreshToken());
    }

    /**
     * 修改当前登录用户密码。
     */
    @Operation(summary = "修改密码")
    @PostMapping("/password/change")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.success(null);
    }

    private AuthRequestContext buildContext(HttpServletRequest httpRequest) {
        return new AuthRequestContext(
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        );
    }
}

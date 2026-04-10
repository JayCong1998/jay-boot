package com.jaycong.boot.rest.site.controller;

import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.modules.auth.dto.AuthRequestContext;
import com.jaycong.boot.modules.auth.dto.SiteAuthSessionResponse;
import com.jaycong.boot.modules.auth.dto.SiteAuthUserView;
import com.jaycong.boot.modules.auth.dto.SiteLoginRequest;
import com.jaycong.boot.modules.auth.dto.SiteLogoutRequest;
import com.jaycong.boot.modules.auth.dto.SiteRegisterRequest;
import com.jaycong.boot.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端认证控制器，提供注册、登录、当前用户与登出接口。
 */
@RestController
@RequestMapping("/api/user/auth")
@Validated
@Tag(name = "用户端认证", description = "用户端注册、登录、当前用户与登出")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户注册并返回会话信息。
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResponse<SiteAuthSessionResponse> register(@Valid @RequestBody SiteRegisterRequest request,
                                                         HttpServletRequest httpRequest) {
        return new ApiResponse<>(200, authService.registerForSite(request, buildContext(httpRequest)), "注册成功", true);
    }

    /**
     * 用户登录并返回会话信息。
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<SiteAuthSessionResponse> login(@Valid @RequestBody SiteLoginRequest request,
                                                      HttpServletRequest httpRequest) {
        return new ApiResponse<>(200, authService.loginForSite(request, buildContext(httpRequest)), "登录成功", true);
    }

    /**
     * 根据 token 获取当前用户信息。
     */
    @Operation(summary = "获取当前用户")
    @GetMapping("/me")
    public ApiResponse<SiteAuthUserView> me(
            @Parameter(description = "登录 token", required = true)
            @RequestParam("token") String token) {
        return new ApiResponse<>(200, authService.meByToken(token), "获取当前用户成功", true);
    }

    /**
     * 根据 token 退出登录。
     */
    @Operation(summary = "用户退出登录")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody SiteLogoutRequest request) {
        authService.logoutByToken(request.token());
        return new ApiResponse<>(200, null, "退出成功", true);
    }

    private AuthRequestContext buildContext(HttpServletRequest httpRequest) {
        return new AuthRequestContext(
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        );
    }
}

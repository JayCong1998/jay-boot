package com.jaycong.boot.rest.site.controller;

import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.modules.mail.dto.EmailCodeSendRequest;
import com.jaycong.boot.modules.mail.dto.EmailCodeSendResultView;
import com.jaycong.boot.modules.mail.dto.EmailCodeVerifyRequest;
import com.jaycong.boot.modules.mail.dto.EmailCodeVerifyResultView;
import com.jaycong.boot.modules.mail.service.VerifyCodeService;
import com.jaycong.boot.modules.ratelimit.annotation.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/auth/email-code")
@Validated
@Tag(name = "邮箱验证码", description = "发送与校验邮箱验证码")
public class EmailCodeController {

    private final VerifyCodeService verifyCodeService;

    public EmailCodeController(VerifyCodeService verifyCodeService) {
        this.verifyCodeService = verifyCodeService;
    }

    @Operation(summary = "发送邮箱验证码")
    @RateLimit(qps = 2, burst = 5, dimension = RateLimit.Dimension.IP, message = "验证码发送太频繁，请稍后重试")
    @PostMapping("/send")
    public ApiResponse<EmailCodeSendResultView> send(@Valid @RequestBody EmailCodeSendRequest request) {
        return ApiResponse.success(verifyCodeService.sendCode(request));
    }

    @Operation(summary = "校验邮箱验证码")
    @RateLimit(qps = 10, burst = 20, dimension = RateLimit.Dimension.IP, message = "验证码校验太频繁，请稍后重试")
    @PostMapping("/verify")
    public ApiResponse<EmailCodeVerifyResultView> verify(@Valid @RequestBody EmailCodeVerifyRequest request) {
        return ApiResponse.success(verifyCodeService.verifyCode(request));
    }
}


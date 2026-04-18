package com.jaycong.boot.rest.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.modules.mail.dto.AdminMailNotificationSendRequest;
import com.jaycong.boot.modules.mail.dto.AdminMailNotificationSendResultView;
import com.jaycong.boot.modules.mail.service.MailNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/mails/notifications")
@Validated
@Tag(name = "邮件通知发送", description = "基于模板发送系统通知")
@SaCheckRole(value = {"admin", "super_admin"}, mode = SaMode.OR)
public class AdminMailNotificationController {

    private final MailNotificationService mailNotificationService;

    public AdminMailNotificationController(MailNotificationService mailNotificationService) {
        this.mailNotificationService = mailNotificationService;
    }

    @Operation(summary = "发送系统通知邮件")
    @PostMapping("/send")
    public ApiResponse<AdminMailNotificationSendResultView> send(@Valid @RequestBody AdminMailNotificationSendRequest request) {
        return ApiResponse.success(mailNotificationService.send(request));
    }
}


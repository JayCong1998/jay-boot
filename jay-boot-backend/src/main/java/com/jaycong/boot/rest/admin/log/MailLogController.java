package com.jaycong.boot.rest.admin.log;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.mail.constant.MailBizType;
import com.jaycong.boot.modules.mail.constant.MailSendStatus;
import com.jaycong.boot.modules.mail.dto.AdminMailSendLogItemView;
import com.jaycong.boot.modules.mail.dto.AdminMailSendLogPageRequest;
import com.jaycong.boot.modules.mail.service.MailSendLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/mails/logs")
@Tag(name = "邮件日志管理", description = "邮件发送日志查询与重试")
@SaCheckRole(value = {"admin", "super_admin"}, mode = SaMode.OR)
public class MailLogController {

    private final MailSendLogService mailSendLogService;

    public MailLogController(MailSendLogService mailSendLogService) {
        this.mailSendLogService = mailSendLogService;
    }

    @Operation(summary = "分页查询邮件日志")
    @GetMapping("/page")
    public ApiResponse<PageResult<AdminMailSendLogItemView>> page(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) MailBizType bizType,
            @RequestParam(required = false) String sceneCode,
            @RequestParam(required = false) String templateCode,
            @RequestParam(required = false) String recipientEmail,
            @RequestParam(required = false) MailSendStatus status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        AdminMailSendLogPageRequest request = new AdminMailSendLogPageRequest(
                page, pageSize, bizType, sceneCode, templateCode, recipientEmail, status, startTime, endTime
        );
        return ApiResponse.success(mailSendLogService.page(request));
    }

    @Operation(summary = "获取邮件日志详情")
    @GetMapping("/{id}")
    public ApiResponse<AdminMailSendLogItemView> getById(@PathVariable Long id) {
        return ApiResponse.success(mailSendLogService.getById(id));
    }

    @Operation(summary = "重试发送邮件")
    @PostMapping("/{id}/retry")
    public ApiResponse<Void> retry(@PathVariable Long id) {
        mailSendLogService.retry(id);
        return ApiResponse.success(null);
    }
}


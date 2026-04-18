package com.jaycong.boot.rest.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.mail.constant.MailStatus;
import com.jaycong.boot.modules.mail.dto.AdminMailChannelCreateRequest;
import com.jaycong.boot.modules.mail.dto.AdminMailChannelItemView;
import com.jaycong.boot.modules.mail.dto.AdminMailChannelPageRequest;
import com.jaycong.boot.modules.mail.dto.AdminMailChannelStatusUpdateRequest;
import com.jaycong.boot.modules.mail.dto.AdminMailChannelUpdateRequest;
import com.jaycong.boot.modules.mail.service.MailChannelService;
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
@RequestMapping("/api/admin/mails/channels")
@Validated
@Tag(name = "邮件通道管理", description = "邮件通道配置管理")
@SaCheckRole(value = {"admin", "super_admin"}, mode = SaMode.OR)
public class AdminMailChannelController {

    private final MailChannelService mailChannelService;

    public AdminMailChannelController(MailChannelService mailChannelService) {
        this.mailChannelService = mailChannelService;
    }

    @Operation(summary = "分页查询邮件通道")
    @GetMapping("/page")
    public ApiResponse<PageResult<AdminMailChannelItemView>> page(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MailStatus status) {
        return ApiResponse.success(mailChannelService.page(new AdminMailChannelPageRequest(page, pageSize, keyword, status)));
    }

    @Operation(summary = "获取邮件通道详情")
    @GetMapping("/{id}")
    public ApiResponse<AdminMailChannelItemView> getById(@PathVariable Long id) {
        return ApiResponse.success(mailChannelService.getById(id));
    }

    @Operation(summary = "创建邮件通道")
    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody AdminMailChannelCreateRequest request) {
        mailChannelService.create(request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "更新邮件通道")
    @PostMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody AdminMailChannelUpdateRequest request) {
        mailChannelService.update(id, request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "更新邮件通道状态")
    @PostMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody AdminMailChannelStatusUpdateRequest request) {
        mailChannelService.updateStatus(id, request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "删除邮件通道")
    @PostMapping("/{id}/delete")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        mailChannelService.delete(id);
        return ApiResponse.success(null);
    }
}


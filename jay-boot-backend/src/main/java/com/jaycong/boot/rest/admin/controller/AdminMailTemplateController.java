package com.jaycong.boot.rest.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.mail.constant.MailBizType;
import com.jaycong.boot.modules.mail.constant.MailStatus;
import com.jaycong.boot.modules.mail.dto.AdminMailTemplateCreateRequest;
import com.jaycong.boot.modules.mail.dto.AdminMailTemplateItemView;
import com.jaycong.boot.modules.mail.dto.AdminMailTemplatePageRequest;
import com.jaycong.boot.modules.mail.dto.AdminMailTemplateStatusUpdateRequest;
import com.jaycong.boot.modules.mail.dto.AdminMailTemplateUpdateRequest;
import com.jaycong.boot.modules.mail.dto.MailTemplatePreviewRequest;
import com.jaycong.boot.modules.mail.dto.MailTemplatePreviewView;
import com.jaycong.boot.modules.mail.service.MailTemplateService;
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
@RequestMapping("/api/admin/mails/templates")
@Validated
@Tag(name = "邮件模板管理", description = "邮件模板配置管理")
@SaCheckRole(value = {"admin", "super_admin"}, mode = SaMode.OR)
public class AdminMailTemplateController {

    private final MailTemplateService mailTemplateService;

    public AdminMailTemplateController(MailTemplateService mailTemplateService) {
        this.mailTemplateService = mailTemplateService;
    }

    @Operation(summary = "分页查询邮件模板")
    @GetMapping("/page")
    public ApiResponse<PageResult<AdminMailTemplateItemView>> page(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MailBizType bizType,
            @RequestParam(required = false) String sceneCode,
            @RequestParam(required = false) MailStatus status) {
        return ApiResponse.success(mailTemplateService.page(
                new AdminMailTemplatePageRequest(page, pageSize, keyword, bizType, sceneCode, status)));
    }

    @Operation(summary = "获取邮件模板详情")
    @GetMapping("/{id}")
    public ApiResponse<AdminMailTemplateItemView> getById(@PathVariable Long id) {
        return ApiResponse.success(mailTemplateService.getById(id));
    }

    @Operation(summary = "创建邮件模板")
    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody AdminMailTemplateCreateRequest request) {
        mailTemplateService.create(request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "更新邮件模板")
    @PostMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody AdminMailTemplateUpdateRequest request) {
        mailTemplateService.update(id, request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "更新邮件模板状态")
    @PostMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody AdminMailTemplateStatusUpdateRequest request) {
        mailTemplateService.updateStatus(id, request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "删除邮件模板")
    @PostMapping("/{id}/delete")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        mailTemplateService.delete(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "预览模板渲染结果")
    @PostMapping("/preview")
    public ApiResponse<MailTemplatePreviewView> preview(@Valid @RequestBody MailTemplatePreviewRequest request) {
        return ApiResponse.success(mailTemplateService.preview(request.templateCode(), request.variables()));
    }
}


package com.jaycong.boot.rest.admin.log;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.log.dto.BatchDeleteRequest;
import com.jaycong.boot.modules.log.dto.ErrorLogItemView;
import com.jaycong.boot.modules.log.dto.ErrorLogQueryRequest;

import com.jaycong.boot.modules.log.service.ErrorLogService;
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
@RequestMapping("/api/admin/logs/errors")
@Validated
@Tag(name = "异常日志管理", description = "异常日志列表、详情、删除")
@SaCheckRole(value = {"admin", "super_admin"}, mode = SaMode.OR)
public class ErrorLogController {

    private final ErrorLogService errorLogService;

    public ErrorLogController(ErrorLogService errorLogService) {
        this.errorLogService = errorLogService;
    }

    @Operation(summary = "分页查询异常日志")
    @GetMapping("/page")
    public ApiResponse<PageResult<ErrorLogItemView>> page(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String requestPath,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        ErrorLogQueryRequest request = new ErrorLogQueryRequest(
                page, pageSize, keyword, requestPath, userId, startTime, endTime);
        return ApiResponse.success(errorLogService.page(request));
    }

    @Operation(summary = "获取异常日志详情")
    @GetMapping("/{id}")
    public ApiResponse<ErrorLogItemView> getById(@PathVariable Long id) {
        return ApiResponse.success(errorLogService.getById(id));
    }

    @Operation(summary = "删除异常日志")
    @PostMapping("/{id}/delete")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        errorLogService.deleteById(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "批量删除异常日志")
    @PostMapping("/batch-delete")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody BatchDeleteRequest request) {
        errorLogService.batchDelete(request.ids());
        return ApiResponse.success(null);
    }
}

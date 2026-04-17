package com.jaycong.boot.rest.admin.log;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.log.dto.BatchDeleteRequest;
import com.jaycong.boot.modules.log.dto.RequestLogItemView;
import com.jaycong.boot.modules.log.dto.RequestLogQueryRequest;

import com.jaycong.boot.modules.log.service.RequestLogService;
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
@RequestMapping("/api/admin/logs/requests")
@Validated
@Tag(name = "请求日志管理", description = "请求日志列表、详情、删除")
@SaCheckRole(value = {"admin", "super_admin"}, mode = SaMode.OR)
public class RequestLogController {

    private final RequestLogService requestLogService;

    public RequestLogController(RequestLogService requestLogService) {
        this.requestLogService = requestLogService;
    }

    @Operation(summary = "分页查询请求日志")
    @GetMapping("/page")
    public ApiResponse<PageResult<RequestLogItemView>> page(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) Integer statusCode,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        RequestLogQueryRequest request = new RequestLogQueryRequest(
                page, pageSize, keyword, method, statusCode, userId, startTime, endTime);
        return ApiResponse.success(requestLogService.page(request));
    }

    @Operation(summary = "获取请求日志详情")
    @GetMapping("/{id}")
    public ApiResponse<RequestLogItemView> getById(@PathVariable Long id) {
        return ApiResponse.success(requestLogService.getById(id));
    }

    @Operation(summary = "删除请求日志")
    @PostMapping("/{id}/delete")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        requestLogService.deleteById(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "批量删除请求日志")
    @PostMapping("/batch-delete")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody BatchDeleteRequest request) {
        requestLogService.batchDelete(request.ids());
        return ApiResponse.success(null);
    }
}

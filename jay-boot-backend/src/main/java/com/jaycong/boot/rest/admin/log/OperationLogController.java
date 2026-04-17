package com.jaycong.boot.rest.admin.log;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.log.dto.OperationLogVO;
import com.jaycong.boot.modules.log.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/logs/operations")
@Tag(name = "操作日志管理", description = "操作日志列表、详情")
@SaCheckRole(value = {"admin", "super_admin"}, mode = SaMode.OR)
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Operation(summary = "分页查询操作日志")
    @GetMapping("/page")
    public ApiResponse<PageResult<OperationLogVO>> page(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return ApiResponse.success(operationLogService.page(page, pageSize, module, userId, startTime, endTime));
    }

    @Operation(summary = "获取操作日志详情")
    @GetMapping("/{id}")
    public ApiResponse<OperationLogVO> getById(@PathVariable Long id) {
        return ApiResponse.success(operationLogService.getById(id));
    }
}

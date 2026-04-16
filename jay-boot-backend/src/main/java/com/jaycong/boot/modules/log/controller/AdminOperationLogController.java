package com.jaycong.boot.modules.log.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jaycong.boot.common.web.Result;
import com.jaycong.boot.modules.log.dto.OperationLogQueryDTO;
import com.jaycong.boot.modules.log.dto.OperationLogVO;
import com.jaycong.boot.modules.log.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端-操作日志")
@RestController
@RequestMapping("/api/admin/logs/operations")
@RequiredArgsConstructor
public class AdminOperationLogController {

    private final OperationLogService operationLogService;

    @Operation(summary = "分页查询操作日志")
    @GetMapping
    public Result<IPage<OperationLogVO>> queryPage(OperationLogQueryDTO query) {
        return Result.ok(operationLogService.queryPage(query));
    }

    @Operation(summary = "查询操作日志详情")
    @GetMapping("/{id}")
    public Result<OperationLogVO> getById(@PathVariable Long id) {
        return Result.ok(operationLogService.getById(id));
    }
}

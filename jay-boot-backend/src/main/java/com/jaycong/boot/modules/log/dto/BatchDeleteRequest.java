package com.jaycong.boot.modules.log.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

/**
 * 批量删除请求。
 * 用于批量删除日志记录。
 *
 * @param ids 要删除的记录ID列表
 */
public record BatchDeleteRequest(
        @NotEmpty(message = "ID列表不能为空")
        List<Long> ids
) {}

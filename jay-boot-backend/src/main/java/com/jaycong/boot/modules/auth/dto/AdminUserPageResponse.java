package com.jaycong.boot.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "管理端用户分页响应")
public record AdminUserPageResponse(
        @Schema(description = "当前页记录")
        List<AdminUserItemView> records,
        @Schema(description = "总记录数", example = "58")
        long total,
        @Schema(description = "当前页码", example = "1")
        long page,
        @Schema(description = "每页条数", example = "10")
        long pageSize
) {
}


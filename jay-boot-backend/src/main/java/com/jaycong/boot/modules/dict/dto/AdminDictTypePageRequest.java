package com.jaycong.boot.modules.dict.dto;

import com.jaycong.boot.common.constant.enums.DictStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理端字典类型分页请求")
public record AdminDictTypePageRequest(
        @Schema(description = "页码", example = "1")
        Integer page,
        @Schema(description = "每页条数", example = "10")
        Integer pageSize,
        @Schema(description = "关键字，匹配类型编码/名称", example = "plan")
        String keyword,
        @Schema(description = "状态")
        DictStatus status
) {
}


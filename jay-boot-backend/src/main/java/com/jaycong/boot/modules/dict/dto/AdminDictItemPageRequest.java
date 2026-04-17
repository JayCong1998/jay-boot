package com.jaycong.boot.modules.dict.dto;

import com.jaycong.boot.common.constant.enums.DictStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理端字典项分页请求")
public record AdminDictItemPageRequest(
        @Schema(description = "页码", example = "1")
        Integer page,
        @Schema(description = "每页条数", example = "10")
        Integer pageSize,
        @Schema(description = "类型编码", example = "plan_status")
        String typeCode,
        @Schema(description = "关键字，匹配字典项名称/值", example = "ACTIVE")
        String keyword,
        @Schema(description = "状态")
        DictStatus status
) {
}

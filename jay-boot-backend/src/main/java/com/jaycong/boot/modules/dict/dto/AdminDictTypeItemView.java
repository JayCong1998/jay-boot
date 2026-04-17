package com.jaycong.boot.modules.dict.dto;

import com.jaycong.boot.common.constant.enums.DictStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理端字典类型列表项")
public record AdminDictTypeItemView(
        @Schema(description = "类型ID，序列化为字符串", example = "1951000000000000001", type = "string")
        Long id,
        @Schema(description = "类型编码", example = "plan_status")
        String typeCode,
        @Schema(description = "类型名称", example = "套餐状态")
        String typeName,
        @Schema(description = "状态")
        DictStatus status,
        @Schema(description = "描述")
        String description,
        @Schema(description = "更新时间", example = "2026-04-17T10:20:00")
        String updatedTime
) {
}


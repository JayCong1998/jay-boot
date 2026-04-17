package com.jaycong.boot.modules.dict.dto;

import com.jaycong.boot.common.constant.enums.DictStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理端字典项列表项")
public record AdminDictItemView(
        @Schema(description = "字典项ID，序列化为字符串", example = "1951000000001000031", type = "string")
        Long id,
        @Schema(description = "字典类型编码", example = "plan_status")
        String typeCode,
        @Schema(description = "字典项编码", example = "ACTIVE")
        String itemCode,
        @Schema(description = "字典项名称", example = "启用")
        String itemLabel,
        @Schema(description = "字典项值", example = "ACTIVE")
        String itemValue,
        @Schema(description = "排序值", example = "10")
        Integer sort,
        @Schema(description = "颜色", example = "success")
        String color,
        @Schema(description = "扩展 JSON")
        String extJson,
        @Schema(description = "状态")
        DictStatus status,
        @Schema(description = "更新时间", example = "2026-04-17T10:20:00")
        String updatedTime
) {
}


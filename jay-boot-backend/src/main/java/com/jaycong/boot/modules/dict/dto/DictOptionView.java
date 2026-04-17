package com.jaycong.boot.modules.dict.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "字典选项")
public record DictOptionView(
        @Schema(description = "字典项值", example = "ACTIVE")
        String value,
        @Schema(description = "字典项显示名称", example = "启用")
        String label,
        @Schema(description = "排序值（升序）", example = "10")
        Integer sort,
        @Schema(description = "展示颜色", example = "success")
        String color,
        @Schema(description = "扩展 JSON")
        String extJson
) {
}

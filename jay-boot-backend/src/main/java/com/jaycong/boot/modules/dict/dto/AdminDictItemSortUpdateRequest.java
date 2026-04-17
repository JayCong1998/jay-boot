package com.jaycong.boot.modules.dict.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理端更新字典项排序请求")
public record AdminDictItemSortUpdateRequest(
        @Schema(description = "排序值", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
        @NotNull(message = "排序值不能为空")
        Integer sort
) {
}


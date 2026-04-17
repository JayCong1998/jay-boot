package com.jaycong.boot.modules.dict.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理端批量调整字典项排序请求")
public record AdminDictItemBatchSortAdjustRequest(
        @Schema(description = "字典项ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "字典项ID列表不能为空")
        List<Long> ids,
        @Schema(description = "排序增量，可为负数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
        @NotNull(message = "排序增量不能为空")
        Integer delta
) {
}

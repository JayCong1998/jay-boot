package com.jaycong.boot.modules.dict.dto;

import com.jaycong.boot.common.constant.enums.DictStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理端批量更新字典项状态请求")
public record AdminDictItemBatchStatusUpdateRequest(
        @Schema(description = "字典项ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "字典项ID列表不能为空")
        List<Long> ids,
        @Schema(description = "目标状态", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "状态不能为空")
        DictStatus status
) {
}

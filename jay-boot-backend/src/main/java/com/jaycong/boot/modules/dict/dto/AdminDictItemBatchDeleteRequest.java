package com.jaycong.boot.modules.dict.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "管理端批量删除字典项请求")
public record AdminDictItemBatchDeleteRequest(
        @Schema(description = "字典项ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "字典项ID列表不能为空")
        List<Long> ids
) {
}

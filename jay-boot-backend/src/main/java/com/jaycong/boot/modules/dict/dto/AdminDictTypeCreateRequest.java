package com.jaycong.boot.modules.dict.dto;

import com.jaycong.boot.common.constant.enums.DictStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "管理端创建字典类型请求")
public record AdminDictTypeCreateRequest(
        @Schema(description = "类型编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "plan_status")
        @NotBlank(message = "类型编码不能为空")
        @Size(max = 64, message = "类型编码长度不能超过64")
        @Pattern(regexp = "^[a-z0-9_]+$", message = "类型编码仅支持小写字母、数字和下划线")
        String typeCode,
        @Schema(description = "类型名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "套餐状态")
        @NotBlank(message = "类型名称不能为空")
        @Size(max = 64, message = "类型名称长度不能超过64")
        String typeName,
        @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "状态不能为空")
        DictStatus status,
        @Schema(description = "描述")
        @Size(max = 255, message = "描述长度不能超过255")
        String description
) {
}


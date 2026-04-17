package com.jaycong.boot.modules.dict.dto;

import com.jaycong.boot.common.constant.enums.DictStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "管理端创建字典项请求")
public record AdminDictItemCreateRequest(
        @Schema(description = "字典类型编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "plan_status")
        @NotBlank(message = "字典类型编码不能为空")
        @Size(max = 64, message = "字典类型编码长度不能超过64")
        @Pattern(regexp = "^[a-z0-9_]+$", message = "字典类型编码仅支持小写字母、数字和下划线")
        String typeCode,
        @Schema(description = "字典项编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "ACTIVE")
        @NotBlank(message = "字典项编码不能为空")
        @Size(max = 64, message = "字典项编码长度不能超过64")
        @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "字典项编码仅支持字母、数字和下划线")
        String itemCode,
        @Schema(description = "字典项名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "启用")
        @NotBlank(message = "字典项名称不能为空")
        @Size(max = 128, message = "字典项名称长度不能超过128")
        String itemLabel,
        @Schema(description = "字典项值", requiredMode = Schema.RequiredMode.REQUIRED, example = "ACTIVE")
        @NotBlank(message = "字典项值不能为空")
        @Size(max = 128, message = "字典项值长度不能超过128")
        String itemValue,
        @Schema(description = "排序值", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
        @NotNull(message = "排序值不能为空")
        Integer sort,
        @Schema(description = "颜色")
        @Size(max = 32, message = "颜色长度不能超过32")
        String color,
        @Schema(description = "扩展 JSON")
        String extJson,
        @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "状态不能为空")
        DictStatus status
) {
}


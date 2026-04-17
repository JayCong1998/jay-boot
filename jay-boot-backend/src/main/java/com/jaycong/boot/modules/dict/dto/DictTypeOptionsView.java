package com.jaycong.boot.modules.dict.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "字典类型及其选项列表")
public record DictTypeOptionsView(
        @Schema(description = "字典类型编码", example = "admin_user_status")
        String typeCode,
        @Schema(description = "选项列表")
        List<DictOptionView> options
) {
}


package com.jaycong.boot.rest.site.controller;

import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.modules.dict.dto.DictOptionView;
import com.jaycong.boot.modules.dict.dto.DictTypeOptionsView;
import com.jaycong.boot.modules.dict.service.PublicDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/dict")
@Validated
@Tag(name = "公共字典", description = "面向前端展示层的只读字典查询接口")
public class PublicDictController {

    private final PublicDictService publicDictService;

    public PublicDictController(PublicDictService publicDictService) {
        this.publicDictService = publicDictService;
    }

    @Operation(summary = "按类型编码查询字典选项")
    @GetMapping("/options")
    public ApiResponse<List<DictOptionView>> listOptions(
            @Parameter(description = "字典类型编码，如：admin_user_status")
            @RequestParam("typeCode") String typeCode) {
        return ApiResponse.success(publicDictService.listOptions(typeCode));
    }

    @Operation(summary = "批量查询多个字典类型的选项")
    @GetMapping("/options/batch")
    public ApiResponse<List<DictTypeOptionsView>> listBatchOptions(
            @Parameter(description = "字典类型编码，多个用逗号分隔")
            @RequestParam("typeCodes") String typeCodes) {
        return ApiResponse.success(publicDictService.listBatchOptions(typeCodes));
    }
}


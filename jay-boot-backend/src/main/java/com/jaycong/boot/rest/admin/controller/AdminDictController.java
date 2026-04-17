package com.jaycong.boot.rest.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.jaycong.boot.common.constant.enums.DictStatus;
import com.jaycong.boot.common.web.ApiResponse;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.dict.dto.AdminDictItemBatchDeleteRequest;
import com.jaycong.boot.modules.dict.dto.AdminDictItemBatchSortAdjustRequest;
import com.jaycong.boot.modules.dict.dto.AdminDictItemBatchStatusUpdateRequest;
import com.jaycong.boot.modules.dict.dto.AdminDictItemCreateRequest;
import com.jaycong.boot.modules.dict.dto.AdminDictItemPageRequest;
import com.jaycong.boot.modules.dict.dto.AdminDictItemSortUpdateRequest;
import com.jaycong.boot.modules.dict.dto.AdminDictItemStatusUpdateRequest;
import com.jaycong.boot.modules.dict.dto.AdminDictItemUpdateRequest;
import com.jaycong.boot.modules.dict.dto.AdminDictItemView;
import com.jaycong.boot.modules.dict.dto.AdminDictTypeCreateRequest;
import com.jaycong.boot.modules.dict.dto.AdminDictTypeItemView;
import com.jaycong.boot.modules.dict.dto.AdminDictTypePageRequest;
import com.jaycong.boot.modules.dict.dto.AdminDictTypeStatusUpdateRequest;
import com.jaycong.boot.modules.dict.dto.AdminDictTypeUpdateRequest;
import com.jaycong.boot.modules.dict.service.AdminDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dicts")
@Validated
@Tag(name = "管理端字典管理", description = "字典类型与字典项维护")
@SaCheckRole(value = {"admin", "super_admin"}, mode = SaMode.OR)
public class AdminDictController {

    private final AdminDictService adminDictService;

    public AdminDictController(AdminDictService adminDictService) {
        this.adminDictService = adminDictService;
    }

    @Operation(summary = "分页查询字典类型")
    @GetMapping("/types/page")
    public ApiResponse<PageResult<AdminDictTypeItemView>> pageTypes(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) DictStatus status) {
        return ApiResponse.success(adminDictService.pageTypes(new AdminDictTypePageRequest(page, pageSize, keyword, status)));
    }

    @Operation(summary = "获取字典类型详情")
    @GetMapping("/types/{id}")
    public ApiResponse<AdminDictTypeItemView> getTypeById(@PathVariable Long id) {
        return ApiResponse.success(adminDictService.getTypeById(id));
    }

    @Operation(summary = "创建字典类型")
    @PostMapping("/types")
    public ApiResponse<Void> createType(@Valid @RequestBody AdminDictTypeCreateRequest request) {
        adminDictService.createType(request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "更新字典类型")
    @PostMapping("/types/{id}")
    public ApiResponse<Void> updateType(@PathVariable Long id, @Valid @RequestBody AdminDictTypeUpdateRequest request) {
        adminDictService.updateType(id, request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "更新字典类型状态")
    @PostMapping("/types/{id}/status")
    public ApiResponse<Void> updateTypeStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminDictTypeStatusUpdateRequest request) {
        adminDictService.updateTypeStatus(id, request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "删除字典类型")
    @PostMapping("/types/{id}/delete")
    public ApiResponse<Void> deleteType(@PathVariable Long id) {
        adminDictService.deleteType(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "分页查询字典项")
    @GetMapping("/items/page")
    public ApiResponse<PageResult<AdminDictItemView>> pageItems(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String typeCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) DictStatus status) {
        return ApiResponse.success(adminDictService.pageItems(
                new AdminDictItemPageRequest(page, pageSize, typeCode, keyword, status)));
    }

    @Operation(summary = "获取字典项详情")
    @GetMapping("/items/{id}")
    public ApiResponse<AdminDictItemView> getItemById(@PathVariable Long id) {
        return ApiResponse.success(adminDictService.getItemById(id));
    }

    @Operation(summary = "创建字典项")
    @PostMapping("/items")
    public ApiResponse<Void> createItem(@Valid @RequestBody AdminDictItemCreateRequest request) {
        adminDictService.createItem(request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "更新字典项")
    @PostMapping("/items/{id}")
    public ApiResponse<Void> updateItem(@PathVariable Long id, @Valid @RequestBody AdminDictItemUpdateRequest request) {
        adminDictService.updateItem(id, request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "更新字典项状态")
    @PostMapping("/items/{id}/status")
    public ApiResponse<Void> updateItemStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminDictItemStatusUpdateRequest request) {
        adminDictService.updateItemStatus(id, request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "更新字典项排序")
    @PostMapping("/items/{id}/sort")
    public ApiResponse<Void> updateItemSort(@PathVariable Long id, @Valid @RequestBody AdminDictItemSortUpdateRequest request) {
        adminDictService.updateItemSort(id, request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "批量更新字典项状态")
    @PostMapping("/items/batch-status")
    public ApiResponse<Void> batchUpdateItemStatus(@Valid @RequestBody AdminDictItemBatchStatusUpdateRequest request) {
        adminDictService.batchUpdateItemStatus(request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "批量删除字典项")
    @PostMapping("/items/batch-delete")
    public ApiResponse<Void> batchDeleteItems(@Valid @RequestBody AdminDictItemBatchDeleteRequest request) {
        adminDictService.batchDeleteItems(request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "批量调整字典项排序")
    @PostMapping("/items/batch-sort-adjust")
    public ApiResponse<Void> batchAdjustItemSort(@Valid @RequestBody AdminDictItemBatchSortAdjustRequest request) {
        adminDictService.batchAdjustItemSort(request);
        return ApiResponse.success(null);
    }

    @Operation(summary = "删除字典项")
    @PostMapping("/items/{id}/delete")
    public ApiResponse<Void> deleteItem(@PathVariable Long id) {
        adminDictService.deleteItem(id);
        return ApiResponse.success(null);
    }
}

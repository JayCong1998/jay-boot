package com.jaycong.boot.modules.dict.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaycong.boot.common.constant.enums.DictStatus;
import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import com.jaycong.boot.common.util.ValidateUtil;
import com.jaycong.boot.common.web.PageResult;
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
import com.jaycong.boot.modules.dict.entity.DictItemEntity;
import com.jaycong.boot.modules.dict.entity.DictTypeEntity;
import com.jaycong.boot.modules.dict.mapper.DictItemMapper;
import com.jaycong.boot.modules.dict.mapper.DictTypeMapper;
import com.jaycong.boot.modules.log.annotation.OperationLog;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminDictService {

    private final DictTypeMapper dictTypeMapper;
    private final DictItemMapper dictItemMapper;

    public AdminDictService(DictTypeMapper dictTypeMapper, DictItemMapper dictItemMapper) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictItemMapper = dictItemMapper;
    }

    public PageResult<AdminDictTypeItemView> pageTypes(AdminDictTypePageRequest request) {
        long pageNo = request.page() == null || request.page() < 1 ? 1 : request.page();
        long pageSize = request.pageSize() == null || request.pageSize() < 1 ? 10 : request.pageSize();

        LambdaQueryWrapper<DictTypeEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.keyword())) {
            String keyword = request.keyword().trim();
            wrapper.and(w -> w.like(DictTypeEntity::getTypeCode, keyword).or().like(DictTypeEntity::getTypeName, keyword));
        }
        if (request.status() != null) {
            wrapper.eq(DictTypeEntity::getStatus, request.status().name());
        }
        wrapper.orderByAsc(DictTypeEntity::getTypeCode).orderByDesc(DictTypeEntity::getUpdatedTime);

        Page<DictTypeEntity> page = dictTypeMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<AdminDictTypeItemView> records = page.getRecords().stream().map(this::toTypeItemView).toList();
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public AdminDictTypeItemView getTypeById(Long id) {
        return toTypeItemView(requireType(id));
    }

    @Transactional
    @OperationLog(module = "字典管理", action = "创建字典类型", detail = "创建字典类型：#{#request.typeCode}")
    public void createType(AdminDictTypeCreateRequest request) {
        DictTypeEntity entity = new DictTypeEntity();
        entity.setTypeCode(normalizeTypeCode(request.typeCode()));
        entity.setTypeName(normalizeTypeName(request.typeName()));
        entity.setStatus(request.status().name());
        entity.setDescription(normalizeOptionalText(request.description(), 255));
        insertType(entity);
    }

    @Transactional
    @OperationLog(module = "字典管理", action = "更新字典类型", detail = "更新字典类型ID：#{#id}")
    public void updateType(Long id, AdminDictTypeUpdateRequest request) {
        DictTypeEntity entity = requireType(id);
        entity.setTypeName(normalizeTypeName(request.typeName()));
        entity.setStatus(request.status().name());
        entity.setDescription(normalizeOptionalText(request.description(), 255));
        updateTypeEntity(entity);
    }

    @Transactional
    @OperationLog(module = "字典管理", action = "修改字典类型状态", detail = "字典类型ID：#{#id}, 状态：#{#request.status.name()}")
    public void updateTypeStatus(Long id, AdminDictTypeStatusUpdateRequest request) {
        DictTypeEntity entity = requireType(id);
        entity.setStatus(request.status().name());
        updateTypeEntity(entity);
    }

    @Transactional
    @OperationLog(module = "字典管理", action = "删除字典类型", detail = "删除字典类型ID：#{#id}")
    public void deleteType(Long id) {
        DictTypeEntity entity = requireType(id);
        long itemCount = countItemsByTypeCode(entity.getTypeCode());
        if (itemCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "请先删除该类型下的字典项");
        }
        dictTypeMapper.deleteById(id);
    }

    public PageResult<AdminDictItemView> pageItems(AdminDictItemPageRequest request) {
        long pageNo = request.page() == null || request.page() < 1 ? 1 : request.page();
        long pageSize = request.pageSize() == null || request.pageSize() < 1 ? 10 : request.pageSize();

        LambdaQueryWrapper<DictItemEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.typeCode())) {
            wrapper.eq(DictItemEntity::getTypeCode, normalizeTypeCode(request.typeCode()));
        }
        if (StringUtils.hasText(request.keyword())) {
            String keyword = request.keyword().trim();
            wrapper.and(w -> w.like(DictItemEntity::getItemCode, keyword)
                    .or().like(DictItemEntity::getItemLabel, keyword)
                    .or().like(DictItemEntity::getItemValue, keyword));
        }
        if (request.status() != null) {
            wrapper.eq(DictItemEntity::getStatus, request.status().name());
        }
        wrapper.orderByAsc(DictItemEntity::getTypeCode)
                .orderByAsc(DictItemEntity::getSort)
                .orderByDesc(DictItemEntity::getUpdatedTime);

        Page<DictItemEntity> page = dictItemMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<AdminDictItemView> records = page.getRecords().stream().map(this::toDictItemView).toList();
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public AdminDictItemView getItemById(Long id) {
        return toDictItemView(requireItem(id));
    }

    @Transactional
    @OperationLog(module = "字典管理", action = "创建字典项", detail = "创建字典项：#{#request.typeCode}/#{#request.itemCode}")
    public void createItem(AdminDictItemCreateRequest request) {
        String typeCode = normalizeTypeCode(request.typeCode());
        ensureTypeExists(typeCode);

        DictItemEntity entity = new DictItemEntity();
        entity.setTypeCode(typeCode);
        entity.setItemCode(normalizeItemCode(request.itemCode()));
        entity.setItemLabel(normalizeItemLabel(request.itemLabel()));
        entity.setItemValue(normalizeItemValue(request.itemValue()));
        entity.setSort(request.sort());
        entity.setColor(normalizeOptionalText(request.color(), 32));
        entity.setExtJson(normalizeOptionalText(request.extJson(), null));
        entity.setStatus(request.status().name());
        insertItem(entity);
    }

    @Transactional
    @OperationLog(module = "字典管理", action = "更新字典项", detail = "更新字典项ID：#{#id}")
    public void updateItem(Long id, AdminDictItemUpdateRequest request) {
        DictItemEntity entity = requireItem(id);
        entity.setItemLabel(normalizeItemLabel(request.itemLabel()));
        entity.setItemValue(normalizeItemValue(request.itemValue()));
        entity.setSort(request.sort());
        entity.setColor(normalizeOptionalText(request.color(), 32));
        entity.setExtJson(normalizeOptionalText(request.extJson(), null));
        entity.setStatus(request.status().name());
        updateItemEntity(entity);
    }

    @Transactional
    @OperationLog(module = "字典管理", action = "修改字典项状态", detail = "字典项ID：#{#id}, 状态：#{#request.status.name()}")
    public void updateItemStatus(Long id, AdminDictItemStatusUpdateRequest request) {
        DictItemEntity entity = requireItem(id);
        entity.setStatus(request.status().name());
        updateItemEntity(entity);
    }

    @Transactional
    @OperationLog(module = "字典管理", action = "修改字典项排序", detail = "字典项ID：#{#id}, 排序：#{#request.sort}")
    public void updateItemSort(Long id, AdminDictItemSortUpdateRequest request) {
        DictItemEntity entity = requireItem(id);
        entity.setSort(request.sort());
        updateItemEntity(entity);
    }

    @Transactional
    @OperationLog(module = "字典管理", action = "删除字典项", detail = "删除字典项ID：#{#id}")
    public void deleteItem(Long id) {
        requireItem(id);
        dictItemMapper.deleteById(id);
    }

    private void ensureTypeExists(String typeCode) {
        LambdaQueryWrapper<DictTypeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictTypeEntity::getTypeCode, typeCode).last("limit 1");
        DictTypeEntity entity = dictTypeMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "字典类型不存在");
        }
    }

    private DictTypeEntity requireType(Long id) {
        DictTypeEntity entity = dictTypeMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "字典类型不存在");
        }
        return entity;
    }

    private DictItemEntity requireItem(Long id) {
        DictItemEntity entity = dictItemMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "字典项不存在");
        }
        return entity;
    }

    private long countItemsByTypeCode(String typeCode) {
        LambdaQueryWrapper<DictItemEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictItemEntity::getTypeCode, typeCode);
        return dictItemMapper.selectCount(wrapper);
    }

    private void insertType(DictTypeEntity entity) {
        try {
            dictTypeMapper.insert(entity);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ErrorCode.CONFLICT, "字典类型编码已存在");
        }
    }

    private void updateTypeEntity(DictTypeEntity entity) {
        try {
            dictTypeMapper.updateById(entity);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ErrorCode.CONFLICT, "字典类型编码已存在");
        }
    }

    private void insertItem(DictItemEntity entity) {
        try {
            dictItemMapper.insert(entity);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ErrorCode.CONFLICT, "字典项编码已存在");
        }
    }

    private void updateItemEntity(DictItemEntity entity) {
        try {
            dictItemMapper.updateById(entity);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ErrorCode.CONFLICT, "字典项编码已存在");
        }
    }

    private String normalizeTypeCode(String typeCode) {
        String normalized = typeCode == null ? null : typeCode.trim().toLowerCase(Locale.ROOT);
        ValidateUtil.notBlank(normalized, "类型编码不能为空");
        ValidateUtil.maxLength(normalized, 64, "类型编码长度不能超过64");
        ValidateUtil.matches(normalized, "^[a-z0-9_]+$", "类型编码仅支持小写字母、数字和下划线");
        return normalized;
    }

    private String normalizeTypeName(String typeName) {
        String normalized = typeName == null ? null : typeName.trim();
        ValidateUtil.notBlank(normalized, "类型名称不能为空");
        ValidateUtil.maxLength(normalized, 64, "类型名称长度不能超过64");
        return normalized;
    }

    private String normalizeItemCode(String itemCode) {
        String normalized = itemCode == null ? null : itemCode.trim();
        ValidateUtil.notBlank(normalized, "字典项编码不能为空");
        ValidateUtil.maxLength(normalized, 64, "字典项编码长度不能超过64");
        ValidateUtil.matches(normalized, "^[A-Za-z0-9_]+$", "字典项编码仅支持字母、数字和下划线");
        return normalized;
    }

    private String normalizeItemLabel(String itemLabel) {
        String normalized = itemLabel == null ? null : itemLabel.trim();
        ValidateUtil.notBlank(normalized, "字典项名称不能为空");
        ValidateUtil.maxLength(normalized, 128, "字典项名称长度不能超过128");
        return normalized;
    }

    private String normalizeItemValue(String itemValue) {
        String normalized = itemValue == null ? null : itemValue.trim();
        ValidateUtil.notBlank(normalized, "字典项值不能为空");
        ValidateUtil.maxLength(normalized, 128, "字典项值长度不能超过128");
        return normalized;
    }

    private String normalizeOptionalText(String value, Integer maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        if (maxLength != null) {
            ValidateUtil.maxLength(normalized, maxLength, "文本长度不能超过" + maxLength);
        }
        return normalized;
    }

    private AdminDictTypeItemView toTypeItemView(DictTypeEntity entity) {
        String updatedTime = entity.getUpdatedTime() == null ? null : entity.getUpdatedTime().toString();
        return new AdminDictTypeItemView(
                entity.getId(),
                entity.getTypeCode(),
                entity.getTypeName(),
                parseStatus(entity.getStatus()),
                entity.getDescription(),
                updatedTime
        );
    }

    private AdminDictItemView toDictItemView(DictItemEntity entity) {
        String updatedTime = entity.getUpdatedTime() == null ? null : entity.getUpdatedTime().toString();
        return new AdminDictItemView(
                entity.getId(),
                entity.getTypeCode(),
                entity.getItemCode(),
                entity.getItemLabel(),
                entity.getItemValue(),
                entity.getSort(),
                entity.getColor(),
                entity.getExtJson(),
                parseStatus(entity.getStatus()),
                updatedTime
        );
    }

    private DictStatus parseStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        return DictStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
    }
}

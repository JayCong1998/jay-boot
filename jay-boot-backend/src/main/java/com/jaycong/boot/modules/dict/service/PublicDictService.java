package com.jaycong.boot.modules.dict.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jaycong.boot.common.constant.enums.DictStatus;
import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import com.jaycong.boot.modules.dict.dto.DictOptionView;
import com.jaycong.boot.modules.dict.dto.DictTypeOptionsView;
import com.jaycong.boot.modules.dict.entity.DictItemEntity;
import com.jaycong.boot.modules.dict.entity.DictTypeEntity;
import com.jaycong.boot.modules.dict.mapper.DictItemMapper;
import com.jaycong.boot.modules.dict.mapper.DictTypeMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PublicDictService {

    private static final Pattern TYPE_CODE_PATTERN = Pattern.compile("^[a-z0-9_]+$");
    private static final int MAX_BATCH_TYPE_SIZE = 20;

    private final DictTypeMapper dictTypeMapper;
    private final DictItemMapper dictItemMapper;

    public PublicDictService(DictTypeMapper dictTypeMapper, DictItemMapper dictItemMapper) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictItemMapper = dictItemMapper;
    }

    public List<DictOptionView> listOptions(String rawTypeCode) {
        String typeCode = normalizeTypeCode(rawTypeCode);
        if (!containsEnabledType(typeCode)) {
            return List.of();
        }
        return loadOptionsMap(Set.of(typeCode)).getOrDefault(typeCode, List.of());
    }

    public List<DictTypeOptionsView> listBatchOptions(String rawTypeCodes) {
        List<String> typeCodes = parseTypeCodes(rawTypeCodes);
        Map<String, List<DictOptionView>> optionsMap = loadOptionsMap(new LinkedHashSet<>(typeCodes));
        return typeCodes.stream()
                .map(typeCode -> new DictTypeOptionsView(typeCode, optionsMap.getOrDefault(typeCode, List.of())))
                .toList();
    }

    private String normalizeTypeCode(String rawTypeCode) {
        if (!StringUtils.hasText(rawTypeCode)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "typeCode 不能为空");
        }
        String normalized = rawTypeCode.trim().toLowerCase(Locale.ROOT);
        if (!TYPE_CODE_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "typeCode 格式不合法，仅支持小写字母、数字和下划线");
        }
        return normalized;
    }

    private List<String> parseTypeCodes(String rawTypeCodes) {
        if (!StringUtils.hasText(rawTypeCodes)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "typeCodes 不能为空");
        }

        LinkedHashSet<String> typeCodeSet = new LinkedHashSet<>();
        for (String part : rawTypeCodes.split(",")) {
            if (!StringUtils.hasText(part)) {
                continue;
            }
            typeCodeSet.add(normalizeTypeCode(part));
        }

        if (typeCodeSet.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "typeCodes 不能为空");
        }
        if (typeCodeSet.size() > MAX_BATCH_TYPE_SIZE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "typeCodes 数量不能超过 " + MAX_BATCH_TYPE_SIZE + " 个");
        }
        return new ArrayList<>(typeCodeSet);
    }

    private boolean containsEnabledType(String typeCode) {
        LambdaQueryWrapper<DictTypeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(DictTypeEntity::getId)
                .eq(DictTypeEntity::getTypeCode, typeCode)
                .eq(DictTypeEntity::getStatus, DictStatus.ENABLED.value())
                .last("limit 1");
        return dictTypeMapper.selectOne(wrapper) != null;
    }

    private Map<String, List<DictOptionView>> loadOptionsMap(Set<String> requestedTypeCodes) {
        if (requestedTypeCodes.isEmpty()) {
            return Map.of();
        }

        LambdaQueryWrapper<DictTypeEntity> typeWrapper = new LambdaQueryWrapper<>();
        typeWrapper.select(DictTypeEntity::getTypeCode)
                .in(DictTypeEntity::getTypeCode, requestedTypeCodes)
                .eq(DictTypeEntity::getStatus, DictStatus.ENABLED.value());
        List<DictTypeEntity> enabledTypes = dictTypeMapper.selectList(typeWrapper);
        if (enabledTypes.isEmpty()) {
            return Map.of();
        }

        Set<String> enabledTypeCodes = enabledTypes.stream()
                .map(DictTypeEntity::getTypeCode)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        LambdaQueryWrapper<DictItemEntity> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(DictItemEntity::getTypeCode, enabledTypeCodes)
                .eq(DictItemEntity::getStatus, DictStatus.ENABLED.value())
                .orderByAsc(DictItemEntity::getTypeCode)
                .orderByAsc(DictItemEntity::getSort)
                .orderByAsc(DictItemEntity::getId);

        List<DictItemEntity> items = dictItemMapper.selectList(itemWrapper);
        Map<String, List<DictOptionView>> optionsMap = new LinkedHashMap<>();
        for (DictItemEntity item : items) {
            optionsMap.computeIfAbsent(item.getTypeCode(), key -> new ArrayList<>())
                    .add(toOption(item));
        }
        return optionsMap;
    }

    private DictOptionView toOption(DictItemEntity item) {
        return new DictOptionView(
                item.getItemValue(),
                item.getItemLabel(),
                item.getSort(),
                item.getColor(),
                item.getExtJson()
        );
    }
}

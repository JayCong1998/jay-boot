package com.jaycong.boot.modules.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.log.config.LogProperties;
import com.jaycong.boot.modules.log.dto.ErrorLogItemView;
import com.jaycong.boot.modules.log.dto.ErrorLogQueryRequest;
import com.jaycong.boot.modules.log.entity.ErrorLogEntity;
import com.jaycong.boot.modules.log.mapper.ErrorLogMapper;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ErrorLogService {

    private final ErrorLogMapper errorLogMapper;
    private final LogProperties logProperties;

    public ErrorLogService(ErrorLogMapper errorLogMapper, LogProperties logProperties) {
        this.errorLogMapper = errorLogMapper;
        this.logProperties = logProperties;
    }

    public void save(ErrorLogEntity entity) {
        errorLogMapper.insert(entity);
        cleanupOldRecords();
    }

    public PageResult<ErrorLogItemView> page(ErrorLogQueryRequest request) {
        LambdaQueryWrapper<ErrorLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.isNotBlank(request.getKeyword()), w ->
                w.like(ErrorLogEntity::getExceptionClass, request.getKeyword())
                 .or()
                 .like(ErrorLogEntity::getExceptionMessage, request.getKeyword()));
        wrapper.like(StringUtils.isNotBlank(request.getRequestPath()), ErrorLogEntity::getRequestPath, request.getRequestPath());
        wrapper.eq(request.getUserId() != null, ErrorLogEntity::getUserId, request.getUserId());
        wrapper.ge(request.getStartTime() != null, ErrorLogEntity::getCreatedTime, parseDateTime(request.getStartTime()));
        wrapper.le(request.getEndTime() != null, ErrorLogEntity::getCreatedTime, parseDateTime(request.getEndTime()));
        wrapper.orderByDesc(ErrorLogEntity::getCreatedTime);

        IPage<ErrorLogEntity> page = errorLogMapper.selectPage(
                new Page<>(request.getPage(), request.getPageSize()), wrapper);

        List<ErrorLogItemView> records = page.getRecords().stream()
                .map(this::toView)
                .toList();

        return new PageResult<>(records, (int) page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public ErrorLogItemView getById(Long id) {
        ErrorLogEntity entity = errorLogMapper.selectById(id);
        return entity != null ? toView(entity) : null;
    }

    public void deleteById(Long id) {
        errorLogMapper.deleteById(id);
    }

    public void batchDelete(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            errorLogMapper.deleteBatchIds(ids);
        }
    }

    private void cleanupOldRecords() {
        int maxCount = logProperties.getError().getMaxCount();
        Long totalCount = errorLogMapper.selectCount(null);
        if (totalCount > maxCount) {
            int deleteCount = (int) (totalCount - maxCount + 1000);
            LambdaQueryWrapper<ErrorLogEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(ErrorLogEntity::getCreatedTime);
            wrapper.last("LIMIT " + deleteCount);
            List<ErrorLogEntity> oldRecords = errorLogMapper.selectList(wrapper);
            for (ErrorLogEntity entity : oldRecords) {
                errorLogMapper.deleteById(entity.getId());
            }
        }
    }

    private ErrorLogItemView toView(ErrorLogEntity entity) {
        ErrorLogItemView view = new ErrorLogItemView();
        BeanUtils.copyProperties(entity, view);
        return view;
    }

    private LocalDateTime parseDateTime(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        try {
            return LocalDateTime.parse(str, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }
}

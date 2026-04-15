package com.jaycong.boot.modules.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.log.config.LogProperties;
import com.jaycong.boot.modules.log.dto.RequestLogItemView;
import com.jaycong.boot.modules.log.dto.RequestLogQueryRequest;
import com.jaycong.boot.modules.log.entity.RequestLogEntity;
import com.jaycong.boot.modules.log.mapper.RequestLogMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RequestLogService {

    private final RequestLogMapper requestLogMapper;
    private final LogProperties logProperties;

    public RequestLogService(RequestLogMapper requestLogMapper, LogProperties logProperties) {
        this.requestLogMapper = requestLogMapper;
        this.logProperties = logProperties;
    }

    public void save(RequestLogEntity entity) {
        requestLogMapper.insert(entity);
        cleanupOldRecords();
    }

    public PageResult<RequestLogItemView> page(RequestLogQueryRequest request) {
        LambdaQueryWrapper<RequestLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(request.getKeyword()), RequestLogEntity::getPath, request.getKeyword());
        wrapper.eq(StringUtils.isNotBlank(request.getMethod()), RequestLogEntity::getMethod, request.getMethod());
        wrapper.eq(request.getStatusCode() != null, RequestLogEntity::getStatusCode, request.getStatusCode());
        wrapper.eq(request.getUserId() != null, RequestLogEntity::getUserId, request.getUserId());
        wrapper.ge(request.getStartTime() != null, RequestLogEntity::getCreatedTime, parseDateTime(request.getStartTime()));
        wrapper.le(request.getEndTime() != null, RequestLogEntity::getCreatedTime, parseDateTime(request.getEndTime()));
        wrapper.orderByDesc(RequestLogEntity::getCreatedTime);

        IPage<RequestLogEntity> page = requestLogMapper.selectPage(
                new Page<>(request.getPage(), request.getPageSize()), wrapper);

        List<RequestLogItemView> records = page.getRecords().stream()
                .map(this::toView)
                .toList();

        return new PageResult<>(records, (int) page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    public RequestLogItemView getById(Long id) {
        RequestLogEntity entity = requestLogMapper.selectById(id);
        return entity != null ? toView(entity) : null;
    }

    public void deleteById(Long id) {
        requestLogMapper.deleteById(id);
    }

    public void batchDelete(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            requestLogMapper.deleteBatchIds(ids);
        }
    }

    public RequestLogEntity findByRequestId(String requestId) {
        if (StringUtils.isBlank(requestId)) {
            return null;
        }
        LambdaQueryWrapper<RequestLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RequestLogEntity::getRequestId, requestId);
        return requestLogMapper.selectOne(wrapper);
    }

    private void cleanupOldRecords() {
        int maxCount = logProperties.getRequest().getMaxCount();
        Long totalCount = requestLogMapper.selectCount(null);
        if (totalCount > maxCount) {
            int deleteCount = (int) (totalCount - maxCount + 1000);
            LambdaQueryWrapper<RequestLogEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(RequestLogEntity::getCreatedTime);
            wrapper.last("LIMIT " + deleteCount);
            List<RequestLogEntity> oldRecords = requestLogMapper.selectList(wrapper);
            for (RequestLogEntity entity : oldRecords) {
                requestLogMapper.deleteById(entity.getId());
            }
        }
    }

    private RequestLogItemView toView(RequestLogEntity entity) {
        RequestLogItemView view = new RequestLogItemView();
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

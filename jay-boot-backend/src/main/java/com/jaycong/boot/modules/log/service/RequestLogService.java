package com.jaycong.boot.modules.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.log.config.LogProperties;
import com.jaycong.boot.modules.log.dto.RequestLogItemView;
import com.jaycong.boot.modules.log.dto.RequestLogQueryRequest;
import com.jaycong.boot.modules.log.entity.RequestLogEntity;
import com.jaycong.boot.modules.log.mapper.RequestLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 请求日志服务类。
 * 提供请求日志的保存、查询和删除功能。
 */
@Service
public class RequestLogService {

    private final RequestLogMapper requestLogMapper;
    private final LogProperties logProperties;

    public RequestLogService(RequestLogMapper requestLogMapper, LogProperties logProperties) {
        this.requestLogMapper = requestLogMapper;
        this.logProperties = logProperties;
    }

    /**
     * 保存请求日志。
     *
     * @param entity 请求日志实体
     */
    public void save(RequestLogEntity entity) {
        requestLogMapper.insert(entity);
        cleanupOldRecords();
    }

    /**
     * 分页查询请求日志。
     *
     * @param request 查询请求参数
     * @return 分页结果
     */
    public PageResult<RequestLogItemView> page(RequestLogQueryRequest request) {
        LambdaQueryWrapper<RequestLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(request.keyword()), RequestLogEntity::getPath, request.keyword());
        wrapper.eq(StringUtils.isNotBlank(request.method()), RequestLogEntity::getMethod, request.method());
        wrapper.eq(request.statusCode() != null, RequestLogEntity::getStatusCode, request.statusCode());
        wrapper.eq(request.userId() != null, RequestLogEntity::getUserId, request.userId());
        wrapper.ge(request.startTime() != null, RequestLogEntity::getCreatedTime, parseDateTime(request.startTime()));
        wrapper.le(request.endTime() != null, RequestLogEntity::getCreatedTime, parseDateTime(request.endTime()));
        wrapper.orderByDesc(RequestLogEntity::getCreatedTime);

        Page<RequestLogEntity> page = requestLogMapper.selectPage(
                new Page<>(request.page(), request.pageSize()), wrapper);

        List<RequestLogItemView> records = page.getRecords().stream()
                .map(this::toView)
                .toList();

        return new PageResult<>(records, (int) page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 根据ID获取请求日志详情。
     *
     * @param id 日志ID
     * @return 请求日志视图对象
     */
    public RequestLogItemView getById(Long id) {
        RequestLogEntity entity = requestLogMapper.selectById(id);
        return entity != null ? toView(entity) : null;
    }

    /**
     * 根据ID删除请求日志。
     *
     * @param id 日志ID
     */
    public void deleteById(Long id) {
        requestLogMapper.deleteById(id);
    }

    /**
     * 批量删除请求日志。
     *
     * @param ids 日志ID列表
     */
    public void batchDelete(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            requestLogMapper.deleteBatchIds(ids);
        }
    }

    /**
     * 根据请求ID查询请求日志。
     *
     * @param requestId 请求ID
     * @return 请求日志实体
     */
    public RequestLogEntity findByRequestId(String requestId) {
        if (StringUtils.isBlank(requestId)) {
            return null;
        }
        LambdaQueryWrapper<RequestLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RequestLogEntity::getRequestId, requestId);
        return requestLogMapper.selectOne(wrapper);
    }

    /**
     * 清理过期的日志记录，保持日志数量不超过配置的最大值。
     */
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

    /**
     * 将实体转换为视图对象。
     *
     * @param entity 请求日志实体
     * @return 请求日志视图对象
     */
    private RequestLogItemView toView(RequestLogEntity entity) {
        return new RequestLogItemView(
                entity.getId(),
                entity.getRequestId(),
                entity.getUserId(),
                entity.getUsername(),
                entity.getMethod(),
                entity.getPath(),
                entity.getQueryString(),
                entity.getRequestParams(),
                entity.getStatusCode(),
                entity.getDurationMs(),
                entity.getClientIp(),
                entity.getUserAgent(),
                entity.getCreatedTime()
        );
    }

    /**
     * 解析日期时间字符串。
     *
     * @param str 日期时间字符串
     * @return 解析后的LocalDateTime，解析失败返回null
     */
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

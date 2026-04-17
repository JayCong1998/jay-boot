package com.jaycong.boot.modules.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.log.config.LogProperties;
import com.jaycong.boot.modules.log.dto.ErrorLogItemView;
import com.jaycong.boot.modules.log.dto.ErrorLogQueryRequest;
import com.jaycong.boot.modules.log.entity.ErrorLogEntity;
import com.jaycong.boot.modules.log.mapper.ErrorLogMapper;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 错误日志服务类。
 * 提供错误日志的保存、查询和删除功能。
 */
@Service
public class ErrorLogService {

    private final ErrorLogMapper errorLogMapper;
    private final LogProperties logProperties;

    public ErrorLogService(ErrorLogMapper errorLogMapper, LogProperties logProperties) {
        this.errorLogMapper = errorLogMapper;
        this.logProperties = logProperties;
    }

    /**
     * 保存错误日志。
     *
     * @param entity 错误日志实体
     */
    public void save(ErrorLogEntity entity) {
        errorLogMapper.insert(entity);
        cleanupOldRecords();
    }

    /**
     * 分页查询错误日志。
     *
     * @param request 查询请求参数
     * @return 分页结果
     */
    public PageResult<ErrorLogItemView> page(ErrorLogQueryRequest request) {
        LambdaQueryWrapper<ErrorLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.isNotBlank(request.keyword()), w ->
                w.like(ErrorLogEntity::getExceptionClass, request.keyword())
                 .or()
                 .like(ErrorLogEntity::getExceptionMessage, request.keyword()));
        wrapper.like(StringUtils.isNotBlank(request.requestPath()), ErrorLogEntity::getRequestPath, request.requestPath());
        wrapper.eq(request.userId() != null, ErrorLogEntity::getUserId, request.userId());
        wrapper.ge(request.startTime() != null, ErrorLogEntity::getCreatedTime, parseDateTime(request.startTime()));
        wrapper.le(request.endTime() != null, ErrorLogEntity::getCreatedTime, parseDateTime(request.endTime()));
        wrapper.orderByDesc(ErrorLogEntity::getCreatedTime);

        Page<ErrorLogEntity> page = errorLogMapper.selectPage(
                new Page<>(request.page(), request.pageSize()), wrapper);

        List<ErrorLogItemView> records = page.getRecords().stream()
                .map(this::toView)
                .toList();

        return new PageResult<>(records, (int) page.getTotal(), (int) page.getCurrent(), (int) page.getSize());
    }

    /**
     * 根据ID获取错误日志详情。
     *
     * @param id 日志ID
     * @return 错误日志视图对象
     */
    public ErrorLogItemView getById(Long id) {
        ErrorLogEntity entity = errorLogMapper.selectById(id);
        return entity != null ? toView(entity) : null;
    }

    /**
     * 根据ID删除错误日志。
     *
     * @param id 日志ID
     */
    public void deleteById(Long id) {
        errorLogMapper.deleteById(id);
    }

    /**
     * 批量删除错误日志。
     *
     * @param ids 日志ID列表
     */
    public void batchDelete(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            errorLogMapper.deleteBatchIds(ids);
        }
    }

    /**
     * 清理过期的日志记录，保持日志数量不超过配置的最大值。
     */
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

    /**
     * 将实体转换为视图对象。
     *
     * @param entity 错误日志实体
     * @return 错误日志视图对象
     */
    private ErrorLogItemView toView(ErrorLogEntity entity) {
        return new ErrorLogItemView(
                entity.getId(),
                entity.getRequestId(),
                entity.getUserId(),
                entity.getUsername(),
                entity.getRequestPath(),
                entity.getRequestParams(),
                entity.getClientIp(),
                entity.getExceptionClass(),
                entity.getExceptionMessage(),
                entity.getStackTrace(),
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

package com.jaycong.boot.modules.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.log.dto.OperationLogVO;
import com.jaycong.boot.modules.log.entity.OperationLogEntity;
import com.jaycong.boot.modules.log.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 操作日志服务类。
 * 提供操作日志的记录和查询功能。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 记录操作日志。
     *
     * @param entity 操作日志实体
     */
    public void record(OperationLogEntity entity) {
        try {
            operationLogMapper.insert(entity);
        } catch (Exception e) {
            log.warn("操作日志记录失败: module={}, action={}", entity.getModule(), entity.getAction(), e);
        }
    }

    /**
     * 分页查询操作日志。
     *
     * @param page      页码
     * @param pageSize  每页大小
     * @param module    模块名称
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 分页结果
     */
    public PageResult<OperationLogVO> page(Integer page, Integer pageSize, String module, Long userId, String startTime, String endTime) {
        int currentPage = page != null ? page : DEFAULT_PAGE;
        int currentPageSize = pageSize != null ? pageSize : DEFAULT_PAGE_SIZE;

        Page<OperationLogEntity> pageParam = new Page<>(currentPage, currentPageSize);

        LambdaQueryWrapper<OperationLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(module), OperationLogEntity::getModule, module)
               .eq(userId != null, OperationLogEntity::getUserId, userId)
               .ge(StringUtils.hasText(startTime), OperationLogEntity::getCreatedTime, parseStartTime(startTime))
               .le(StringUtils.hasText(endTime), OperationLogEntity::getCreatedTime, parseEndTime(endTime))
               .orderByDesc(OperationLogEntity::getCreatedTime);

        Page<OperationLogEntity> result = operationLogMapper.selectPage(pageParam, wrapper);
        List<OperationLogVO> records = result.getRecords().stream().map(this::toVO).toList();

        return PageResult.of(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 根据ID获取操作日志详情。
     *
     * @param id 日志ID
     * @return 操作日志视图对象
     */
    public OperationLogVO getById(Long id) {
        OperationLogEntity entity = operationLogMapper.selectById(id);
        return entity != null ? toVO(entity) : null;
    }

    /**
     * 将实体转换为视图对象。
     *
     * @param entity 操作日志实体
     * @return 操作日志视图对象
     */
    private OperationLogVO toVO(OperationLogEntity entity) {
        return new OperationLogVO(
                String.valueOf(entity.getId()),
                entity.getModule(),
                entity.getAction(),
                entity.getDetail(),
                entity.getUserId(),
                entity.getUsername(),
                entity.getClientIp(),
                entity.getRequestId(),
                entity.getCreatedTime() != null ? entity.getCreatedTime().format(DATE_FORMATTER) : null
        );
    }

    /**
     * 解析开始时间字符串为LocalDateTime。
     *
     * @param time 时间字符串（yyyy-MM-dd格式）
     * @return 当天开始时间
     */
    private LocalDateTime parseStartTime(String time) {
        if (!StringUtils.hasText(time)) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(time.substring(0, 10));
            return date.atStartOfDay();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 解析结束时间字符串为LocalDateTime。
     *
     * @param time 时间字符串（yyyy-MM-dd格式）
     * @return 当天结束时间
     */
    private LocalDateTime parseEndTime(String time) {
        if (!StringUtils.hasText(time)) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(time.substring(0, 10));
            return date.atTime(LocalTime.MAX);
        } catch (Exception e) {
            return null;
        }
    }
}

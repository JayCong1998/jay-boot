package com.jaycong.boot.modules.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaycong.boot.modules.log.dto.OperationLogQueryDTO;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void record(OperationLogEntity entity) {
        try {
            operationLogMapper.insert(entity);
        } catch (Exception e) {
            log.warn("操作日志记录失败: module={}, action={}", entity.getModule(), entity.getAction(), e);
        }
    }

    public IPage<OperationLogVO> queryPage(OperationLogQueryDTO query) {
        Page<OperationLogEntity> page = new Page<>(query.getPage(), query.getPageSize());
        
        LambdaQueryWrapper<OperationLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(query.getModule()), OperationLogEntity::getModule, query.getModule())
               .eq(query.getUserId() != null, OperationLogEntity::getUserId, query.getUserId())
               .ge(query.getStartTime() != null, OperationLogEntity::getCreatedTime, parseStartTime(query.getStartTime()))
               .le(query.getEndTime() != null, OperationLogEntity::getCreatedTime, parseEndTime(query.getEndTime()))
               .orderByDesc(OperationLogEntity::getCreatedTime);

        IPage<OperationLogEntity> entityPage = operationLogMapper.selectPage(page, wrapper);

        return entityPage.convert(this::toVO);
    }

    public OperationLogVO getById(Long id) {
        OperationLogEntity entity = operationLogMapper.selectById(id);
        return entity != null ? toVO(entity) : null;
    }

    private OperationLogVO toVO(OperationLogEntity entity) {
        OperationLogVO vo = new OperationLogVO();
        vo.setId(String.valueOf(entity.getId()));
        vo.setModule(entity.getModule());
        vo.setAction(entity.getAction());
        vo.setDetail(entity.getDetail());
        vo.setUserId(entity.getUserId());
        vo.setUsername(entity.getUsername());
        vo.setClientIp(entity.getClientIp());
        vo.setRequestId(entity.getRequestId());
        vo.setCreatedTime(entity.getCreatedTime() != null ? entity.getCreatedTime().format(DATE_FORMATTER) : null);
        return vo;
    }

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

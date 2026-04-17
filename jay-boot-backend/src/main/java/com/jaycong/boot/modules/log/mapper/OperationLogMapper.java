package com.jaycong.boot.modules.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jaycong.boot.modules.log.entity.OperationLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志数据访问接口。
 * 提供操作日志的持久化操作。
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogEntity> {
}

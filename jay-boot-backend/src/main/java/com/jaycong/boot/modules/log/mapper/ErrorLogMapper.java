package com.jaycong.boot.modules.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jaycong.boot.modules.log.entity.ErrorLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 错误日志数据访问接口。
 * 提供错误日志的持久化操作。
 */
@Mapper
public interface ErrorLogMapper extends BaseMapper<ErrorLogEntity> {
}

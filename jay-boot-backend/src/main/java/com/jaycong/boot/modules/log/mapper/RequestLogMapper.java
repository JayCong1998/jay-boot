package com.jaycong.boot.modules.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jaycong.boot.modules.log.entity.RequestLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 请求日志数据访问接口。
 * 提供请求日志的持久化操作。
 */
@Mapper
public interface RequestLogMapper extends BaseMapper<RequestLogEntity> {
}

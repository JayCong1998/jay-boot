package com.jaycong.boot.modules.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jaycong.boot.modules.log.entity.OperationLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogEntity> {
}

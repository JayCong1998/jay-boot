package com.jaycong.boot.modules.billing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jaycong.boot.modules.billing.entity.PlanEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 套餐数据访问接口。
 */
@Mapper
public interface PlanMapper extends BaseMapper<PlanEntity> {
}

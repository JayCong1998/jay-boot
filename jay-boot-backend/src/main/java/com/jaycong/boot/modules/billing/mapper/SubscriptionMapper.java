package com.jaycong.boot.modules.billing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jaycong.boot.modules.billing.entity.SubscriptionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订阅数据访问接口。
 */
@Mapper
public interface SubscriptionMapper extends BaseMapper<SubscriptionEntity> {
}

package com.jaycong.boot.modules.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jaycong.boot.modules.tenant.entity.UserTenantEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserTenantMapper extends BaseMapper<UserTenantEntity> {
}

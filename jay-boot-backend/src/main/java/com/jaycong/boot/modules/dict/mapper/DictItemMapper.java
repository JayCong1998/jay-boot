package com.jaycong.boot.modules.dict.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jaycong.boot.modules.dict.entity.DictItemEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DictItemMapper extends BaseMapper<DictItemEntity> {
}


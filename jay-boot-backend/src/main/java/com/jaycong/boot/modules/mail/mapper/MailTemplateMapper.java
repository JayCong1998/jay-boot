package com.jaycong.boot.modules.mail.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jaycong.boot.modules.mail.entity.MailTemplateEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MailTemplateMapper extends BaseMapper<MailTemplateEntity> {
}


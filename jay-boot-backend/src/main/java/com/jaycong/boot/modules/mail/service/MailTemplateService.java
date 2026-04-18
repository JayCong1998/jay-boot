package com.jaycong.boot.modules.mail.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import com.jaycong.boot.common.util.ValidateUtil;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.log.annotation.OperationLog;
import com.jaycong.boot.modules.mail.constant.MailBizType;
import com.jaycong.boot.modules.mail.constant.MailBodyType;
import com.jaycong.boot.modules.mail.constant.MailSceneCode;
import com.jaycong.boot.modules.mail.constant.MailStatus;
import com.jaycong.boot.modules.mail.dto.AdminMailTemplateCreateRequest;
import com.jaycong.boot.modules.mail.dto.AdminMailTemplateItemView;
import com.jaycong.boot.modules.mail.dto.AdminMailTemplatePageRequest;
import com.jaycong.boot.modules.mail.dto.AdminMailTemplateStatusUpdateRequest;
import com.jaycong.boot.modules.mail.dto.AdminMailTemplateUpdateRequest;
import com.jaycong.boot.modules.mail.dto.MailTemplatePreviewView;
import com.jaycong.boot.modules.mail.entity.MailTemplateEntity;
import com.jaycong.boot.modules.mail.mapper.MailTemplateMapper;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MailTemplateService {

    private final MailTemplateMapper mailTemplateMapper;
    private final TemplateRenderService templateRenderService;
    private final ObjectMapper objectMapper;

    public MailTemplateService(MailTemplateMapper mailTemplateMapper,
                               TemplateRenderService templateRenderService,
                               ObjectMapper objectMapper) {
        this.mailTemplateMapper = mailTemplateMapper;
        this.templateRenderService = templateRenderService;
        this.objectMapper = objectMapper;
    }

    public PageResult<AdminMailTemplateItemView> page(AdminMailTemplatePageRequest request) {
        long pageNo = request.page() == null || request.page() < 1 ? 1 : request.page();
        long pageSize = request.pageSize() == null || request.pageSize() < 1 ? 10 : request.pageSize();
        LambdaQueryWrapper<MailTemplateEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.keyword())) {
            String keyword = request.keyword().trim();
            wrapper.and(w -> w.like(MailTemplateEntity::getTemplateCode, keyword)
                    .or().like(MailTemplateEntity::getTemplateName, keyword)
                    .or().like(MailTemplateEntity::getSceneCode, keyword));
        }
        if (request.bizType() != null) {
            wrapper.eq(MailTemplateEntity::getBizType, request.bizType().value());
        }
        if (StringUtils.hasText(request.sceneCode())) {
            wrapper.eq(MailTemplateEntity::getSceneCode, request.sceneCode().trim().toUpperCase(Locale.ROOT));
        }
        if (request.status() != null) {
            wrapper.eq(MailTemplateEntity::getStatus, request.status().value());
        }
        wrapper.orderByAsc(MailTemplateEntity::getBizType)
                .orderByAsc(MailTemplateEntity::getSceneCode)
                .orderByDesc(MailTemplateEntity::getUpdatedTime);
        Page<MailTemplateEntity> page = mailTemplateMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<AdminMailTemplateItemView> records = page.getRecords().stream().map(this::toView).toList();
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public AdminMailTemplateItemView getById(Long id) {
        return toView(requireById(id));
    }

    @Transactional
    @OperationLog(module = "邮件管理", action = "创建邮件模板", detail = "创建邮件模板：#{#request.templateCode}")
    public void create(AdminMailTemplateCreateRequest request) {
        MailTemplateEntity entity = new MailTemplateEntity();
        entity.setTemplateCode(normalizeCode(request.templateCode()));
        entity.setTemplateName(normalizeName(request.templateName()));
        entity.setBizType(request.bizType().value());
        entity.setSceneCode(normalizeSceneCode(request.bizType(), request.sceneCode()));
        entity.setSubjectTemplate(normalizeSubject(request.subjectTemplate()));
        entity.setBodyTemplate(normalizeBodyTemplate(request.bodyTemplate()));
        entity.setBodyType(request.bodyType().value());
        entity.setVarsSchemaJson(normalizeJson(request.varsSchemaJson(), "变量Schema JSON"));
        entity.setStatus(request.status().value());
        entity.setRemark(normalizeOptionalText(request.remark(), 255));
        tryInsert(entity);
    }

    @Transactional
    @OperationLog(module = "邮件管理", action = "更新邮件模板", detail = "更新邮件模板ID：#{#id}")
    public void update(Long id, AdminMailTemplateUpdateRequest request) {
        MailTemplateEntity entity = requireById(id);
        entity.setTemplateName(normalizeName(request.templateName()));
        entity.setBizType(request.bizType().value());
        entity.setSceneCode(normalizeSceneCode(request.bizType(), request.sceneCode()));
        entity.setSubjectTemplate(normalizeSubject(request.subjectTemplate()));
        entity.setBodyTemplate(normalizeBodyTemplate(request.bodyTemplate()));
        entity.setBodyType(request.bodyType().value());
        entity.setVarsSchemaJson(normalizeJson(request.varsSchemaJson(), "变量Schema JSON"));
        entity.setStatus(request.status().value());
        entity.setRemark(normalizeOptionalText(request.remark(), 255));
        mailTemplateMapper.updateById(entity);
    }

    @Transactional
    @OperationLog(module = "邮件管理", action = "更新邮件模板状态", detail = "更新邮件模板ID：#{#id}, 状态：#{#request.status.value()}")
    public void updateStatus(Long id, AdminMailTemplateStatusUpdateRequest request) {
        MailTemplateEntity entity = requireById(id);
        entity.setStatus(request.status().value());
        mailTemplateMapper.updateById(entity);
    }

    @Transactional
    @OperationLog(module = "邮件管理", action = "删除邮件模板", detail = "删除邮件模板ID：#{#id}")
    public void delete(Long id) {
        requireById(id);
        mailTemplateMapper.deleteById(id);
    }

    public MailTemplateEntity findEnabledByCode(String templateCode) {
        String normalized = normalizeCode(templateCode);
        LambdaQueryWrapper<MailTemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MailTemplateEntity::getTemplateCode, normalized)
                .eq(MailTemplateEntity::getStatus, MailStatus.ENABLED.value())
                .last("limit 1");
        MailTemplateEntity entity = mailTemplateMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "邮件模板不存在或未启用");
        }
        return entity;
    }

    public MailTemplateEntity findEnabledByBizScene(MailBizType bizType, String sceneCode) {
        String normalizedScene = normalizeSceneCode(bizType, sceneCode);
        LambdaQueryWrapper<MailTemplateEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MailTemplateEntity::getBizType, bizType.value())
                .eq(MailTemplateEntity::getSceneCode, normalizedScene)
                .eq(MailTemplateEntity::getStatus, MailStatus.ENABLED.value())
                .orderByDesc(MailTemplateEntity::getUpdatedTime)
                .last("limit 1");
        MailTemplateEntity entity = mailTemplateMapper.selectOne(wrapper);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "场景模板不存在或未启用");
        }
        return entity;
    }

    public MailTemplatePreviewView preview(String templateCode, Map<String, Object> variables) {
        MailTemplateEntity template = findEnabledByCode(templateCode);
        return new MailTemplatePreviewView(
                templateRenderService.render(template.getSubjectTemplate(), variables),
                templateRenderService.render(template.getBodyTemplate(), variables)
        );
    }

    private MailTemplateEntity requireById(Long id) {
        MailTemplateEntity entity = mailTemplateMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "邮件模板不存在");
        }
        return entity;
    }

    private void tryInsert(MailTemplateEntity entity) {
        try {
            mailTemplateMapper.insert(entity);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ErrorCode.CONFLICT, "邮件模板编码已存在");
        }
    }

    private AdminMailTemplateItemView toView(MailTemplateEntity entity) {
        String updatedTime = entity.getUpdatedTime() == null ? null : entity.getUpdatedTime().toString();
        return new AdminMailTemplateItemView(
                entity.getId(),
                entity.getTemplateCode(),
                entity.getTemplateName(),
                MailBizType.fromValue(entity.getBizType()),
                entity.getSceneCode(),
                entity.getSubjectTemplate(),
                entity.getBodyTemplate(),
                MailBodyType.fromValue(entity.getBodyType()),
                entity.getVarsSchemaJson(),
                MailStatus.fromValue(entity.getStatus()),
                entity.getRemark(),
                updatedTime
        );
    }

    private String normalizeCode(String code) {
        String normalized = code == null ? null : code.trim().toLowerCase(Locale.ROOT);
        ValidateUtil.notBlank(normalized, "模板编码不能为空");
        ValidateUtil.maxLength(normalized, 64, "模板编码长度不能超过64");
        ValidateUtil.matches(normalized, "^[a-z0-9_]+$", "模板编码仅支持小写字母、数字和下划线");
        return normalized;
    }

    private String normalizeName(String name) {
        String normalized = name == null ? null : name.trim();
        ValidateUtil.notBlank(normalized, "模板名称不能为空");
        ValidateUtil.maxLength(normalized, 64, "模板名称长度不能超过64");
        return normalized;
    }

    private String normalizeSceneCode(MailBizType bizType, String sceneCode) {
        String normalized = sceneCode == null ? null : sceneCode.trim().toUpperCase(Locale.ROOT);
        ValidateUtil.notBlank(normalized, "场景编码不能为空");
        ValidateUtil.maxLength(normalized, 64, "场景编码长度不能超过64");
        ValidateUtil.matches(normalized, "^[A-Z0-9_]+$", "场景编码仅支持大写字母、数字和下划线");
        if (bizType == MailBizType.VERIFY_CODE) {
            MailSceneCode.fromValue(normalized);
        }
        return normalized;
    }

    private String normalizeSubject(String subject) {
        String normalized = subject == null ? null : subject.trim();
        ValidateUtil.notBlank(normalized, "主题模板不能为空");
        ValidateUtil.maxLength(normalized, 255, "主题模板长度不能超过255");
        return normalized;
    }

    private String normalizeBodyTemplate(String bodyTemplate) {
        String normalized = bodyTemplate == null ? null : bodyTemplate.trim();
        ValidateUtil.notBlank(normalized, "正文模板不能为空");
        return normalized;
    }

    private String normalizeJson(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        try {
            objectMapper.readTree(normalized);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " 格式不合法");
        }
        return normalized;
    }

    private String normalizeOptionalText(String value, Integer maxLength) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        if (maxLength != null) {
            ValidateUtil.maxLength(normalized, maxLength, "文本长度不能超过" + maxLength);
        }
        return normalized;
    }
}


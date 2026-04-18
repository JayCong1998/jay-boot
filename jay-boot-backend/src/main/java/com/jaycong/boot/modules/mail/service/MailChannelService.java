package com.jaycong.boot.modules.mail.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import com.jaycong.boot.common.util.ValidateUtil;
import com.jaycong.boot.common.web.PageResult;
import com.jaycong.boot.modules.log.annotation.OperationLog;
import com.jaycong.boot.modules.mail.constant.MailStatus;
import com.jaycong.boot.modules.mail.dto.AdminMailChannelCreateRequest;
import com.jaycong.boot.modules.mail.dto.AdminMailChannelItemView;
import com.jaycong.boot.modules.mail.dto.AdminMailChannelPageRequest;
import com.jaycong.boot.modules.mail.dto.AdminMailChannelStatusUpdateRequest;
import com.jaycong.boot.modules.mail.dto.AdminMailChannelUpdateRequest;
import com.jaycong.boot.modules.mail.entity.MailChannelEntity;
import com.jaycong.boot.modules.mail.mapper.MailChannelMapper;
import com.jaycong.boot.modules.mail.util.MailEncryptor;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MailChannelService {

    private final MailChannelMapper mailChannelMapper;
    private final MailEncryptor mailEncryptor;

    public MailChannelService(MailChannelMapper mailChannelMapper, MailEncryptor mailEncryptor) {
        this.mailChannelMapper = mailChannelMapper;
        this.mailEncryptor = mailEncryptor;
    }

    public PageResult<AdminMailChannelItemView> page(AdminMailChannelPageRequest request) {
        long pageNo = request.page() == null || request.page() < 1 ? 1 : request.page();
        long pageSize = request.pageSize() == null || request.pageSize() < 1 ? 10 : request.pageSize();
        LambdaQueryWrapper<MailChannelEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(request.keyword())) {
            String keyword = request.keyword().trim();
            wrapper.and(w -> w.like(MailChannelEntity::getChannelCode, keyword)
                    .or().like(MailChannelEntity::getChannelName, keyword)
                    .or().like(MailChannelEntity::getFromEmail, keyword));
        }
        if (request.status() != null) {
            wrapper.eq(MailChannelEntity::getStatus, request.status().value());
        }
        wrapper.orderByAsc(MailChannelEntity::getPriority).orderByDesc(MailChannelEntity::getUpdatedTime);
        Page<MailChannelEntity> page = mailChannelMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<AdminMailChannelItemView> records = page.getRecords().stream().map(this::toView).toList();
        return PageResult.of(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    public AdminMailChannelItemView getById(Long id) {
        return toView(requireById(id));
    }

    @Transactional
    @OperationLog(module = "邮件管理", action = "创建邮件通道", detail = "创建邮件通道：#{#request.channelCode}")
    public void create(AdminMailChannelCreateRequest request) {
        MailChannelEntity entity = new MailChannelEntity();
        entity.setChannelCode(normalizeCode(request.channelCode()));
        entity.setChannelName(normalizeName(request.channelName()));
        entity.setProviderType("SMTP");
        entity.setSmtpHost(normalizeHost(request.smtpHost()));
        entity.setSmtpPort(request.smtpPort());
        entity.setSmtpUsername(normalizeSmtpUsername(request.smtpUsername()));
        entity.setSmtpPasswordCipher(mailEncryptor.encrypt(request.smtpPassword().trim()));
        entity.setTlsMode(request.tlsMode().value());
        entity.setFromName(normalizeFromName(request.fromName()));
        entity.setFromEmail(normalizeEmail(request.fromEmail()));
        entity.setPriority(request.priority());
        entity.setStatus(request.status().value());
        entity.setRemark(normalizeOptionalText(request.remark(), 255));
        tryInsert(entity);
    }

    @Transactional
    @OperationLog(module = "邮件管理", action = "更新邮件通道", detail = "更新邮件通道ID：#{#id}")
    public void update(Long id, AdminMailChannelUpdateRequest request) {
        MailChannelEntity entity = requireById(id);
        entity.setChannelName(normalizeName(request.channelName()));
        entity.setSmtpHost(normalizeHost(request.smtpHost()));
        entity.setSmtpPort(request.smtpPort());
        entity.setSmtpUsername(normalizeSmtpUsername(request.smtpUsername()));
        if (StringUtils.hasText(request.smtpPassword())) {
            entity.setSmtpPasswordCipher(mailEncryptor.encrypt(request.smtpPassword().trim()));
        }
        entity.setTlsMode(request.tlsMode().value());
        entity.setFromName(normalizeFromName(request.fromName()));
        entity.setFromEmail(normalizeEmail(request.fromEmail()));
        entity.setPriority(request.priority());
        entity.setStatus(request.status().value());
        entity.setRemark(normalizeOptionalText(request.remark(), 255));
        mailChannelMapper.updateById(entity);
    }

    @Transactional
    @OperationLog(module = "邮件管理", action = "更新邮件通道状态", detail = "更新邮件通道ID：#{#id}, 状态：#{#request.status.value()}")
    public void updateStatus(Long id, AdminMailChannelStatusUpdateRequest request) {
        MailChannelEntity entity = requireById(id);
        entity.setStatus(request.status().value());
        mailChannelMapper.updateById(entity);
    }

    @Transactional
    @OperationLog(module = "邮件管理", action = "删除邮件通道", detail = "删除邮件通道ID：#{#id}")
    public void delete(Long id) {
        requireById(id);
        mailChannelMapper.deleteById(id);
    }

    public MailChannelEntity pickEnabledChannel() {
        LambdaQueryWrapper<MailChannelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MailChannelEntity::getStatus, MailStatus.ENABLED.value())
                .orderByAsc(MailChannelEntity::getPriority)
                .last("limit 1");
        MailChannelEntity channel = mailChannelMapper.selectOne(wrapper);
        if (channel == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "未配置可用邮件通道");
        }
        return channel;
    }

    public MailChannelEntity findByCode(String channelCode) {
        if (!StringUtils.hasText(channelCode)) {
            return null;
        }
        LambdaQueryWrapper<MailChannelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MailChannelEntity::getChannelCode, channelCode.trim().toLowerCase(Locale.ROOT)).last("limit 1");
        return mailChannelMapper.selectOne(wrapper);
    }

    private MailChannelEntity requireById(Long id) {
        MailChannelEntity entity = mailChannelMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "邮件通道不存在");
        }
        return entity;
    }

    private void tryInsert(MailChannelEntity entity) {
        try {
            mailChannelMapper.insert(entity);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ErrorCode.CONFLICT, "邮件通道编码已存在");
        }
    }

    private AdminMailChannelItemView toView(MailChannelEntity entity) {
        String updatedTime = entity.getUpdatedTime() == null ? null : entity.getUpdatedTime().toString();
        String password = mailEncryptor.decrypt(entity.getSmtpPasswordCipher());
        return new AdminMailChannelItemView(
                entity.getId(),
                entity.getChannelCode(),
                entity.getChannelName(),
                entity.getSmtpHost(),
                entity.getSmtpPort(),
                entity.getSmtpUsername(),
                mailEncryptor.mask(password),
                com.jaycong.boot.modules.mail.constant.MailTlsMode.fromValue(entity.getTlsMode()),
                entity.getFromName(),
                entity.getFromEmail(),
                entity.getPriority(),
                MailStatus.fromValue(entity.getStatus()),
                entity.getRemark(),
                updatedTime
        );
    }

    private String normalizeCode(String code) {
        String normalized = code == null ? null : code.trim().toLowerCase(Locale.ROOT);
        ValidateUtil.notBlank(normalized, "通道编码不能为空");
        ValidateUtil.maxLength(normalized, 64, "通道编码长度不能超过64");
        ValidateUtil.matches(normalized, "^[a-z0-9_]+$", "通道编码仅支持小写字母、数字和下划线");
        return normalized;
    }

    private String normalizeName(String name) {
        String normalized = name == null ? null : name.trim();
        ValidateUtil.notBlank(normalized, "通道名称不能为空");
        ValidateUtil.maxLength(normalized, 64, "通道名称长度不能超过64");
        return normalized;
    }

    private String normalizeHost(String host) {
        String normalized = host == null ? null : host.trim();
        ValidateUtil.notBlank(normalized, "SMTP主机不能为空");
        ValidateUtil.maxLength(normalized, 128, "SMTP主机长度不能超过128");
        return normalized;
    }

    private String normalizeSmtpUsername(String username) {
        String normalized = username == null ? null : username.trim();
        ValidateUtil.notBlank(normalized, "SMTP用户名不能为空");
        ValidateUtil.maxLength(normalized, 128, "SMTP用户名长度不能超过128");
        return normalized;
    }

    private String normalizeFromName(String name) {
        String normalized = name == null ? null : name.trim();
        ValidateUtil.notBlank(normalized, "发件人名称不能为空");
        ValidateUtil.maxLength(normalized, 128, "发件人名称长度不能超过128");
        return normalized;
    }

    private String normalizeEmail(String email) {
        String normalized = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
        ValidateUtil.email(normalized, "发件人邮箱格式不正确");
        ValidateUtil.maxLength(normalized, 128, "发件人邮箱长度不能超过128");
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


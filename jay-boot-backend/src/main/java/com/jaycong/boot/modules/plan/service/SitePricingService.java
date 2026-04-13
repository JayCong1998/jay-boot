package com.jaycong.boot.modules.plan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaycong.boot.common.constant.enums.PlanBillingCycle;
import com.jaycong.boot.common.constant.enums.PlanStatus;
import com.jaycong.boot.common.exception.BusinessException;
import com.jaycong.boot.common.exception.ErrorCode;
import com.jaycong.boot.modules.plan.dto.SitePricingOverviewResponse;
import com.jaycong.boot.modules.plan.entity.PlanEntity;
import com.jaycong.boot.modules.plan.mapper.PlanMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户端定价页服务。
 */
@Service
public class SitePricingService {

    private final PlanMapper planMapper;
    private final ObjectMapper objectMapper;

    public SitePricingService(PlanMapper planMapper, ObjectMapper objectMapper) {
        this.planMapper = planMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取定价页概览数据。
     *
     * @param billingCycleText 计费周期文本（MONTHLY/YEARLY）
     * @return 定价页概览响应
     */
    public SitePricingOverviewResponse getOverview(String billingCycleText) {
        PlanBillingCycle billingCycle = parseBillingCycle(billingCycleText);
        List<PlanProfile> profiles = listPlanProfiles(billingCycle);
        if (profiles.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "当前周期暂无可售套餐");
        }

        String recommendedCode = resolveRecommendedCode(profiles);
        List<SitePricingOverviewResponse.PlanCard> planCards = profiles.stream()
                .map(profile -> toPlanCard(profile, billingCycle, Objects.equals(profile.code(), recommendedCode)))
                .toList();

        return new SitePricingOverviewResponse(
                buildHero(),
                buildCycleOptions(),
                billingCycle,
                planCards,
                buildComparisonRows(profiles),
                buildFaqList(),
                buildFinalCta(),
                Instant.now().toString()
        );
    }

    private PlanBillingCycle parseBillingCycle(String billingCycleText) {
        if (!StringUtils.hasText(billingCycleText)) {
            return PlanBillingCycle.MONTHLY;
        }
        String normalized = billingCycleText.trim().toUpperCase(Locale.ROOT);
        try {
            return PlanBillingCycle.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "billingCycle 仅支持 MONTHLY 或 YEARLY");
        }
    }

    private List<PlanProfile> listPlanProfiles(PlanBillingCycle billingCycle) {
        LambdaQueryWrapper<PlanEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlanEntity::getStatus, PlanStatus.ACTIVE.name())
                .eq(PlanEntity::getBillingCycle, billingCycle.name())
                .orderByAsc(PlanEntity::getPrice)
                .orderByDesc(PlanEntity::getUpdatedTime);

        return planMapper.selectList(wrapper).stream()
                .map(this::toPlanProfile)
                .sorted(Comparator
                        .comparingInt((PlanProfile profile) -> profile.tier().order())
                        .thenComparingLong(PlanProfile::price))
                .toList();
    }

    private PlanProfile toPlanProfile(PlanEntity entity) {
        String code = normalizeCode(entity);
        PlanTier tier = resolveTier(code, entity.getName());
        JsonNode quotaNode = parseQuotaJson(entity.getQuotaJson());

        long monthlyGenerations = firstPositiveLong(
                quotaNode,
                tier.defaultMonthlyGenerations(),
                "monthlyGenerations",
                "generationsMonthly",
                "tokensMonthly",
                "monthlyQuota"
        );
        int seats = (int) firstPositiveLong(
                quotaNode,
                tier.defaultSeats(),
                "seats",
                "members",
                "teamMembers"
        );

        boolean advancedTemplates = firstBoolean(quotaNode, tier.supportsAdvancedTemplates(), "advancedTemplates", "proTemplates");
        boolean batchExport = firstBoolean(quotaNode, tier.supportsBatchExport(), "batchExport", "exportBatch");
        boolean teamCollaboration = firstBoolean(
                quotaNode,
                tier.supportsTeamCollaboration() || seats > 1,
                "teamCollaboration",
                "multiMember"
        );

        List<String> highlights = resolveHighlights(quotaNode, tier, monthlyGenerations, seats, advancedTemplates, teamCollaboration);
        String fitFor = resolveFitFor(tier);

        Long price = entity.getPrice() == null ? 0L : entity.getPrice();
        return new PlanProfile(
                entity.getId(),
                code,
                normalizeName(entity),
                price,
                tier,
                monthlyGenerations,
                seats,
                advancedTemplates,
                batchExport,
                teamCollaboration,
                highlights,
                fitFor
        );
    }

    private String normalizeCode(PlanEntity entity) {
        if (StringUtils.hasText(entity.getCode())) {
            return entity.getCode().trim().toUpperCase(Locale.ROOT);
        }
        return "PLAN_" + entity.getId();
    }

    private String normalizeName(PlanEntity entity) {
        if (StringUtils.hasText(entity.getName())) {
            return entity.getName().trim();
        }
        return normalizeCode(entity);
    }

    private JsonNode parseQuotaJson(String quotaJson) {
        if (!StringUtils.hasText(quotaJson)) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(quotaJson);
        } catch (Exception ex) {
            return objectMapper.createObjectNode();
        }
    }

    private long firstPositiveLong(JsonNode node, long defaultValue, String... keys) {
        for (String key : keys) {
            JsonNode valueNode = node.get(key);
            if (valueNode == null) {
                continue;
            }
            long value = valueNode.asLong(0L);
            if (value > 0) {
                return value;
            }
        }
        return defaultValue;
    }

    private boolean firstBoolean(JsonNode node, boolean defaultValue, String... keys) {
        for (String key : keys) {
            JsonNode valueNode = node.get(key);
            if (valueNode == null) {
                continue;
            }
            if (valueNode.isBoolean()) {
                return valueNode.asBoolean();
            }
            String text = valueNode.asText();
            if (!StringUtils.hasText(text)) {
                continue;
            }
            if ("true".equalsIgnoreCase(text.trim())) {
                return true;
            }
            if ("false".equalsIgnoreCase(text.trim())) {
                return false;
            }
        }
        return defaultValue;
    }

    private List<String> resolveHighlights(
            JsonNode quotaNode,
            PlanTier tier,
            long monthlyGenerations,
            int seats,
            boolean advancedTemplates,
            boolean teamCollaboration
    ) {
        List<String> highlights = new ArrayList<>();
        JsonNode highlightNode = quotaNode.get("highlights");
        if (highlightNode != null && highlightNode.isArray()) {
            for (JsonNode node : highlightNode) {
                String text = node.asText();
                if (StringUtils.hasText(text)) {
                    highlights.add(text.trim());
                }
            }
        }

        if (highlights.isEmpty()) {
            highlights.add(monthlyGenerations + " 次/月生成额度");
            highlights.add(advancedTemplates ? "高级模板与导出交付能力" : "基础模板与历史记录能力");
            if (teamCollaboration) {
                highlights.add("支持 " + seats + " 人协作与权限治理");
            } else {
                highlights.add("适合个人轻量创作场景");
            }
        }

        while (highlights.size() < 3) {
            highlights.add(tier.fallbackHighlight());
        }
        return highlights.stream().limit(3).toList();
    }

    private String resolveFitFor(PlanTier tier) {
        return switch (tier) {
            case FREE -> "个人轻度创作与试用阶段";
            case PRO -> "稳定生产内容的创作者";
            case TEAM -> "多人协作团队与业务负责人";
            case OTHER -> "按业务规模灵活选择";
        };
    }

    private String resolveRecommendedCode(List<PlanProfile> profiles) {
        return profiles.stream()
                .filter(profile -> profile.tier() == PlanTier.PRO)
                .findFirst()
                .or(() -> profiles.stream().max(Comparator.comparingLong(PlanProfile::price)))
                .map(PlanProfile::code)
                .orElse("");
    }

    private SitePricingOverviewResponse.PlanCard toPlanCard(
            PlanProfile profile,
            PlanBillingCycle billingCycle,
            boolean recommended
    ) {
        return new SitePricingOverviewResponse.PlanCard(
                profile.id(),
                profile.code(),
                profile.name(),
                recommended ? "Most Popular" : null,
                profile.price(),
                formatPrice(profile.price()),
                billingCycle == PlanBillingCycle.YEARLY ? "/ 年" : "/ 月",
                profile.fitFor(),
                profile.highlights(),
                recommended,
                profile.tier() == PlanTier.TEAM
        );
    }

    private String formatPrice(long cents) {
        BigDecimal value = BigDecimal.valueOf(cents, 2).stripTrailingZeros();
        return "¥" + value.toPlainString();
    }

    private List<SitePricingOverviewResponse.ComparisonRow> buildComparisonRows(List<PlanProfile> profiles) {
        return List.of(
                buildComparisonRow(
                        "monthly_quota",
                        "月度生成额度",
                        "每个套餐的默认月度生成能力",
                        profiles,
                        profile -> profile.monthlyGenerations() + " 次/月"
                ),
                buildComparisonRow(
                        "advanced_templates",
                        "高级模板",
                        "是否支持高级模板与创作助手",
                        profiles,
                        profile -> profile.advancedTemplates() ? "支持" : "不支持"
                ),
                buildComparisonRow(
                        "batch_export",
                        "批量导出",
                        "是否支持批量导出与交付",
                        profiles,
                        profile -> profile.batchExport() ? "支持" : "不支持"
                ),
                buildComparisonRow(
                        "team_collaboration",
                        "多人协作",
                        "是否支持多人协作与权限管理",
                        profiles,
                        profile -> profile.teamCollaboration() ? "支持（" + profile.seats() + " 人）" : "不支持"
                )
        );
    }

    private SitePricingOverviewResponse.ComparisonRow buildComparisonRow(
            String id,
            String feature,
            String description,
            List<PlanProfile> profiles,
            Function<PlanProfile, String> valueResolver
    ) {
        List<SitePricingOverviewResponse.ComparisonCell> cells = profiles.stream()
                .map(profile -> new SitePricingOverviewResponse.ComparisonCell(profile.code(), valueResolver.apply(profile)))
                .toList();
        return new SitePricingOverviewResponse.ComparisonRow(id, feature, description, cells);
    }

    private SitePricingOverviewResponse.Hero buildHero() {
        return new SitePricingOverviewResponse.Hero(
                "定价透明，按增长阶段选择",
                "订阅定价",
                "通过清晰权益和周期折扣，帮助你更快完成购买决策。",
                "咨询推荐方案",
                "进入支付页"
        );
    }

    private List<SitePricingOverviewResponse.CycleOption> buildCycleOptions() {
        return List.of(
                new SitePricingOverviewResponse.CycleOption(PlanBillingCycle.MONTHLY, "月付", "适合先验证增长节奏"),
                new SitePricingOverviewResponse.CycleOption(PlanBillingCycle.YEARLY, "年付", "相比月付更优惠")
        );
    }

    private List<SitePricingOverviewResponse.FaqItem> buildFaqList() {
        return List.of(
                new SitePricingOverviewResponse.FaqItem(
                        "faq_1",
                        "可以随时升级或降级吗？",
                        "支持按账单周期切换，差价自动折算。"
                ),
                new SitePricingOverviewResponse.FaqItem(
                        "faq_2",
                        "是否支持企业发票？",
                        "支持开具电子发票，支付页可填写抬头与税号。"
                ),
                new SitePricingOverviewResponse.FaqItem(
                        "faq_3",
                        "有退款政策吗？",
                        "首购 7 天内可申请一次退款，具体规则以帮助中心说明为准。"
                )
        );
    }

    private SitePricingOverviewResponse.FinalCta buildFinalCta() {
        return new SitePricingOverviewResponse.FinalCta(
                "从今天开始，让内容产出稳定转化为收入",
                "优先推荐 Pro 档，后续可按团队规模平滑升级。",
                "立即开通",
                "预约演示"
        );
    }

    private PlanTier resolveTier(String code, String name) {
        String source = (StringUtils.hasText(code) ? code : "") + " " + (StringUtils.hasText(name) ? name : "");
        String normalized = source.toUpperCase(Locale.ROOT);
        if (normalized.contains("FREE") || normalized.contains("STARTER") || normalized.contains("入门")) {
            return PlanTier.FREE;
        }
        if (normalized.contains("PRO") || normalized.contains("专业")) {
            return PlanTier.PRO;
        }
        if (normalized.contains("TEAM") || normalized.contains("ENTERPRISE") || normalized.contains("团队")) {
            return PlanTier.TEAM;
        }
        return PlanTier.OTHER;
    }

    private record PlanProfile(
            Long id,
            String code,
            String name,
            long price,
            PlanTier tier,
            long monthlyGenerations,
            int seats,
            boolean advancedTemplates,
            boolean batchExport,
            boolean teamCollaboration,
            List<String> highlights,
            String fitFor
    ) {
    }

    private enum PlanTier {
        FREE(0, 200, 1, false, false, false, "基础创作能力"),
        PRO(1, 1200, 1, true, true, false, "进阶创作与交付能力"),
        TEAM(2, 4000, 5, true, true, true, "团队协作与权限治理"),
        OTHER(9, 600, 1, false, false, false, "可按需扩展能力");

        private final int order;
        private final long defaultMonthlyGenerations;
        private final int defaultSeats;
        private final boolean supportsAdvancedTemplates;
        private final boolean supportsBatchExport;
        private final boolean supportsTeamCollaboration;
        private final String fallbackHighlight;

        PlanTier(
                int order,
                long defaultMonthlyGenerations,
                int defaultSeats,
                boolean supportsAdvancedTemplates,
                boolean supportsBatchExport,
                boolean supportsTeamCollaboration,
                String fallbackHighlight
        ) {
            this.order = order;
            this.defaultMonthlyGenerations = defaultMonthlyGenerations;
            this.defaultSeats = defaultSeats;
            this.supportsAdvancedTemplates = supportsAdvancedTemplates;
            this.supportsBatchExport = supportsBatchExport;
            this.supportsTeamCollaboration = supportsTeamCollaboration;
            this.fallbackHighlight = fallbackHighlight;
        }

        public int order() {
            return order;
        }

        public long defaultMonthlyGenerations() {
            return defaultMonthlyGenerations;
        }

        public int defaultSeats() {
            return defaultSeats;
        }

        public boolean supportsAdvancedTemplates() {
            return supportsAdvancedTemplates;
        }

        public boolean supportsBatchExport() {
            return supportsBatchExport;
        }

        public boolean supportsTeamCollaboration() {
            return supportsTeamCollaboration;
        }

        public String fallbackHighlight() {
            return fallbackHighlight;
        }
    }
}
package com.jaycong.boot.modules.home.dto;

import java.util.List;

/**
 * 首页概览响应。
 *
 * @param hero          首屏文案
 * @param trustBadges   信任背书标签
 * @param sectionTitles 分区标题
 * @param featureCards  核心价值卡片
 * @param kpiCards      社证 KPI 卡片
 * @param faqList       FAQ 列表
 * @param finalCta      底部 CTA
 * @param updatedAt     数据更新时间（ISO）
 */
public record HomeOverviewResponse(
        Hero hero,
        List<String> trustBadges,
        SectionTitles sectionTitles,
        List<FeatureCard> featureCards,
        List<KpiCard> kpiCards,
        List<FaqItem> faqList,
        FinalCta finalCta,
        String updatedAt
) {

    /**
     * 首屏文案与按钮。
     */
    public record Hero(
            String eyebrow,
            String title,
            String description,
            String primaryActionText,
            String secondaryActionText
    ) {
    }

    /**
     * 区块标题。
     */
    public record SectionTitles(
            String features,
            String socialProof,
            String faq
    ) {
    }

    /**
     * 核心价值卡片。
     */
    public record FeatureCard(
            String id,
            String title,
            List<String> points,
            boolean soft
    ) {
    }

    /**
     * KPI 卡片。
     */
    public record KpiCard(
            String id,
            String label,
            String value,
            String desc,
            boolean soft
    ) {
    }

    /**
     * FAQ 项。
     */
    public record FaqItem(
            String id,
            String question,
            String answer
    ) {
    }

    /**
     * 底部 CTA 文案。
     */
    public record FinalCta(
            String title,
            String description,
            String primaryActionText,
            String secondaryActionText
    ) {
    }
}


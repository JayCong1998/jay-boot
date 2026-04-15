package com.jaycong.boot.modules.plan.dto;

import com.jaycong.boot.common.constant.enums.PlanBillingCycle;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "用户端定价页概览响应")
public record SitePricingOverviewResponse(
        @Schema(description = "首屏文案")
        Hero hero,
        @Schema(description = "计费周期选项")
        List<CycleOption> cycleOptions,
        @Schema(description = "当前选中的计费周期")
        PlanBillingCycle selectedCycle,
        @Schema(description = "套餐卡片列表")
        List<PlanCard> planCards,
        @Schema(description = "权益对比行列表")
        List<ComparisonRow> comparisonRows,
        @Schema(description = "常见问题列表")
        List<FaqItem> faqList,
        @Schema(description = "底部行动引导")
        FinalCta finalCta,
        @Schema(description = "数据更新时间（ISO-8601）", example = "2026-04-13T12:00:00.000Z")
        String updatedAt
) {

    @Schema(description = "首屏区域")
    public record Hero(
            @Schema(description = "眉标题", example = "定价透明，按增长阶段选择")
            String eyebrow,
            @Schema(description = "主标题", example = "订阅定价")
            String title,
            @Schema(description = "描述文案")
            String description,
            @Schema(description = "主按钮文案", example = "咨询推荐方案")
            String primaryActionText,
            @Schema(description = "次按钮文案", example = "进入支付页")
            String secondaryActionText
    ) {
    }

    @Schema(description = "计费周期选项")
    public record CycleOption(
            @Schema(description = "周期键值")
            PlanBillingCycle key,
            @Schema(description = "显示名称", example = "月付")
            String label,
            @Schema(description = "提示文案", example = "适合先验证增长节奏")
            String hint
    ) {
    }

    @Schema(description = "套餐卡片")
    public record PlanCard(
            @Schema(description = "套餐ID（字符串返回）", example = "1949000000000000002", type = "string")
            Long id,
            @Schema(description = "套餐编码", example = "PRO_MONTHLY")
            String code,
            @Schema(description = "套餐名称", example = "Pro")
            String name,
            @Schema(description = "角标文案", example = "Most Popular")
            String tag,
            @Schema(description = "套餐价格（单位：分）", example = "12900")
            Long price,
            @Schema(description = "价格展示文案", example = "¥129")
            String priceText,
            @Schema(description = "计费周期展示文案", example = "/ 月")
            String billingCycleText,
            @Schema(description = "适用人群描述")
            String fitFor,
            @Schema(description = "权益亮点列表")
            List<String> highlights,
            @Schema(description = "是否推荐")
            boolean recommended,
            @Schema(description = "是否需要联系销售")
            boolean contactSales
    ) {
    }

    @Schema(description = "权益对比行")
    public record ComparisonRow(
            @Schema(description = "对比行ID", example = "monthly_quota")
            String id,
            @Schema(description = "权益名称", example = "月度生成额度")
            String feature,
            @Schema(description = "权益说明")
            String description,
            @Schema(description = "各套餐对应值")
            List<ComparisonCell> cells
    ) {
    }

    @Schema(description = "权益对比单元")
    public record ComparisonCell(
            @Schema(description = "套餐编码", example = "PRO_MONTHLY")
            String planCode,
            @Schema(description = "权益值文案", example = "1200 次/月")
            String value
    ) {
    }

    @Schema(description = "常见问题项")
    public record FaqItem(
            @Schema(description = "问题ID", example = "faq_1")
            String id,
            @Schema(description = "问题")
            String question,
            @Schema(description = "答案")
            String answer
    ) {
    }

    @Schema(description = "底部行动引导")
    public record FinalCta(
            @Schema(description = "标题")
            String title,
            @Schema(description = "描述")
            String description,
            @Schema(description = "主按钮文案", example = "立即开通")
            String primaryActionText,
            @Schema(description = "次按钮文案", example = "预约演示")
            String secondaryActionText
    ) {
    }
}
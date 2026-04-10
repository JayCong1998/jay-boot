package com.jaycong.boot.modules.home.service;

import com.jaycong.boot.modules.home.dto.HomeCaseItemView;
import com.jaycong.boot.modules.home.dto.HomeOverviewResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 首页内容服务。
 */
@Service
public class HomeService {

    private static final int DEFAULT_CASE_LIMIT = 3;
    private static final int MAX_CASE_LIMIT = 4;

    private static final List<HomeCaseItemView> CASE_ITEMS = List.of(
            new HomeCaseItemView(
                    "case_1",
                    "教育训练营：7 天起号专题",
                    "教育培训",
                    "单周咨询线索 +129%",
                    "用多版本文案 + 结构化话题清单，2 人团队在 7 天内完成 42 条内容上线。"
            ),
            new HomeCaseItemView(
                    "case_2",
                    "本地生活门店：团购拉新脚本",
                    "本地生活",
                    "门店到店率 +37%",
                    "按门店地理位置与客群画像自动生成短视频脚本，减少重复改稿时间。"
            ),
            new HomeCaseItemView(
                    "case_3",
                    "品牌电商：上新周活动页",
                    "电商零售",
                    "活动页转化率 +18%",
                    "基于同款商品历史数据生成 A/B 文案版本，并自动输出交付清单。"
            ),
            new HomeCaseItemView(
                    "case_4",
                    "知识博主：私域课程首发",
                    "内容创作",
                    "预约转化 +26%",
                    "通过“选题-大纲-发布”的流水线模板，一周稳定产出 15 条可发布内容。"
            )
    );

    /**
     * 获取首页概览数据。
     */
    public HomeOverviewResponse getOverview() {
        return new HomeOverviewResponse(
                new HomeOverviewResponse.Hero(
                        "面向内容创业者的增长引擎",
                        "10 秒生成可发布文案",
                        "从选题、生成、迭代到交付，帮助你把 AI 创作能力直接转化为稳定收益。首页目标是“看完即理解，点击就试用”。",
                        "立即免费试用",
                        "查看真实案例"
                ),
                List.of("4.2 万创作者正在使用", "平均节省写作时间 68%", "支持小红书/公众号/短视频"),
                new HomeOverviewResponse.SectionTitles("核心价值", "社证数据", "常见问题"),
                List.of(
                        new HomeOverviewResponse.FeatureCard(
                                "feature_1",
                                "高转化模板",
                                List.of("按平台和场景生成文案结构", "支持热门选题拆解与复刻", "减少从 0 到 1 的思考成本"),
                                false
                        ),
                        new HomeOverviewResponse.FeatureCard(
                                "feature_2",
                                "多版本并行",
                                List.of("一次生成多个风格版本", "便于 A/B 测试提升转化", "保留历史用于持续复盘"),
                                false
                        ),
                        new HomeOverviewResponse.FeatureCard(
                                "feature_3",
                                "结果可交付",
                                List.of("支持链接交付给客户或团队", "支持导出 Markdown 与图片文档", "形成“创作-交付-复购”闭环"),
                                true
                        )
                ),
                List.of(
                        new HomeOverviewResponse.KpiCard(
                                "kpi_1",
                                "累计生成内容",
                                "2,900 万+",
                                "覆盖营销、私域、直播、电商等场景",
                                false
                        ),
                        new HomeOverviewResponse.KpiCard(
                                "kpi_2",
                                "7 日留存",
                                "74%",
                                "核心用户持续使用工作台",
                                false
                        ),
                        new HomeOverviewResponse.KpiCard(
                                "kpi_3",
                                "付费转化",
                                "12.8%",
                                "通过案例页进入的用户转化更高",
                                true
                        )
                ),
                List.of(
                        new HomeOverviewResponse.FaqItem("faq_1", "适合哪些人群？", "个体创作者、内容团队、带货操盘手都可直接使用。"),
                        new HomeOverviewResponse.FaqItem("faq_2", "免费试用包含什么？", "可体验核心模板与基础生成能力，不限登录设备。"),
                        new HomeOverviewResponse.FaqItem("faq_3", "是否支持团队协作？", "支持多人协作、任务共享和结果交付链接。")
                ),
                new HomeOverviewResponse.FinalCta(
                        "准备好把创作效率变成盈利能力了吗？",
                        "现在注册即可开启试用，并获得首月订阅优惠。",
                        "免费开始",
                        "查看套餐"
                ),
                Instant.now().toString()
        );
    }

    /**
     * 获取首页案例列表。
     */
    public List<HomeCaseItemView> getCases(Integer limit) {
        int normalizedLimit = normalizeCaseLimit(limit);
        return CASE_ITEMS.subList(0, normalizedLimit);
    }

    private int normalizeCaseLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_CASE_LIMIT;
        }
        if (limit < 1) {
            return 1;
        }
        return Math.min(limit, MAX_CASE_LIMIT);
    }
}


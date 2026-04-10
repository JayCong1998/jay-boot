package com.jaycong.boot.modules.home.dto;

/**
 * 首页案例卡片。
 *
 * @param id       案例 ID
 * @param title    案例标题
 * @param industry 行业标签
 * @param gain     收益摘要
 * @param summary  案例简介
 */
public record HomeCaseItemView(
        String id,
        String title,
        String industry,
        String gain,
        String summary
) {
}


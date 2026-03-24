package com.zjb.mjgl.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 健康评分计算与风险建议。
 *
 * 说明：本类提供默认公式；后续若要“调参与配置化”，可以把常量下沉到配置或数据库。
 */
public final class HealthScoreCalculator {

    // 归一化常量（建议后续配置化）
    private static final BigDecimal K_USAGE = new BigDecimal("100");
    private static final BigDecimal K_FAULT = new BigDecimal("1.5");
    private static final BigDecimal K_COST_RATE = new BigDecimal("200"); // 元/小时

    // 权重（和需求文档保持一致）
    private static final BigDecimal W_USAGE = new BigDecimal("0.15");
    private static final BigDecimal W_FAULT = new BigDecimal("0.35");
    private static final BigDecimal W_MAINTENANCE = new BigDecimal("0.35");
    private static final BigDecimal W_COST = new BigDecimal("0.15");

    // 风险分级阈值
    private static final int THRESHOLD_EXCELLENT = 85;
    private static final int THRESHOLD_ATTENTION = 70;
    private static final int THRESHOLD_RISK = 50;

    private HealthScoreCalculator() {}

    public static int calcHealthScore(int usageCount,
                                        double productionTimeHours,
                                        int faultCount,
                                        double repairCostTotal,
                                        int maintenanceCompletedCount,
                                        int maintenancePlannedCount) {

        BigDecimal U = BigDecimal.valueOf(Math.max(usageCount, 0));
        BigDecimal T = BigDecimal.valueOf(Math.max(productionTimeHours, 0));
        BigDecimal F = BigDecimal.valueOf(Math.max(faultCount, 0));
        BigDecimal C = BigDecimal.valueOf(Math.max(repairCostTotal, 0));

        BigDecimal MC = BigDecimal.valueOf(Math.max(maintenanceCompletedCount, 0));
        BigDecimal MP = BigDecimal.valueOf(Math.max(maintenancePlannedCount, 0));

        BigDecimal costRate = C.divide(T.max(BigDecimal.ONE), 6, RoundingMode.HALF_UP); // 元/小时

        // 归一化：score 越大越好（health 越高风险越低）
        BigDecimal usageRisk = U.divide(U.add(K_USAGE), 6, RoundingMode.HALF_UP);
        BigDecimal usageScore = BigDecimal.ONE.subtract(usageRisk);

        BigDecimal faultScore = BigDecimal.ONE.subtract(
                F.divide(F.add(K_FAULT), 6, RoundingMode.HALF_UP)
        );

        BigDecimal maintenanceScore = MC.divide(MP.max(BigDecimal.ONE), 6, RoundingMode.HALF_UP);
        BigDecimal costScore = BigDecimal.ONE.divide(
                BigDecimal.ONE.add(costRate.divide(K_COST_RATE, 6, RoundingMode.HALF_UP)),
                6,
                RoundingMode.HALF_UP
        );

        BigDecimal raw = W_USAGE.multiply(usageScore)
                .add(W_FAULT.multiply(faultScore))
                .add(W_MAINTENANCE.multiply(maintenanceScore))
                .add(W_COST.multiply(costScore));

        int score = raw.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).intValue();
        if (score < 0) score = 0;
        if (score > 100) score = 100;
        return score;
    }

    public static String calcRiskLevel(int healthScore) {
        if (healthScore >= THRESHOLD_EXCELLENT) return "优良";
        if (healthScore >= THRESHOLD_ATTENTION) return "关注";
        if (healthScore >= THRESHOLD_RISK) return "风险";
        return "紧急";
    }

    public static List<String> recommendActions(int healthScore,
                                                   BigDecimal maintenanceRatePercent,
                                                   BigDecimal faultRate,
                                                   BigDecimal costRate) {
        // 这里给出“V1 默认建议”，用于 PDF 与管理看板展示
        // 后续可沉淀为规则表/可配置阈值。
        if (healthScore < THRESHOLD_RISK) {
            return Arrays.asList(
                    "优先级：最高",
                    "故障排查：检查故障类型集中度，安排专项检修（必要时停机窗口优化）",
                    "维保缺口：核对保养完成率，确保保养计划按期执行",
                    "成本复盘：核查维修成本构成与原因，评估备件与工艺优化"
            );
        }

        if (maintenanceRatePercent != null
                && maintenanceRatePercent.compareTo(new BigDecimal("80")) < 0
                && faultRate != null
                && faultRate.compareTo(new BigDecimal("0.2")) > 0) {
            return Arrays.asList(
                    "优先级：较高",
                    "维保优化：提高保养优先级/覆盖范围，降低故障率上升趋势",
                    "原因分析：结合故障记录，定位导致频繁故障的工序/部位"
            );
        }

        if (costRate != null && costRate.compareTo(new BigDecimal("200")) > 0) {
            return Arrays.asList(
                    "优先级：中高",
                    "成本控制：复盘维修成本结构（工时/备件/外协），制定成本优化措施",
                    "预防性维护：根据成本热点制定预防性保养重点"
            );
        }

        return Collections.singletonList("优先级：常规（维持现有保养策略，持续监控趋势）");
    }

    public static BigDecimal safePercent(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null) numerator = BigDecimal.ZERO;
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) <= 0) denominator = BigDecimal.ONE;
        return numerator.divide(denominator, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }
}


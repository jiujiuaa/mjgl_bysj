package com.zjb.mjgl.pojo.vo;

import com.zjb.mjgl.pojo.entity.HealthReports;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class HealthReportVO extends HealthReports {
    private String moldCode;
    private String moldName;

    private String riskLevel; // 优良/关注/风险/紧急

    private BigDecimal faultRate; // 故障率（故障次数/使用次数）
    private BigDecimal costRate; // 单位生产时长维修成本（元/小时）

    private List<String> recommendedActions;

    /**
     * PDF 预览/下载 URL（可选，建议调用时生成预签名）
     */
    private String pdfUrl;
}


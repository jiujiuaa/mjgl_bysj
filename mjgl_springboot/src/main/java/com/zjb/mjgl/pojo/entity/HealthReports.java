package com.zjb.mjgl.pojo.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * health_reports 主表：模具健康评估报告
 */
@Data
public class HealthReports {
    private String id;
    private String moldId;
    private String reportTitle;
    private LocalDate reportPeriodStart;
    private LocalDate reportPeriodEnd;

    private Integer totalUsageCount;
    private BigDecimal totalProductionTime; // 小时

    private Integer faultCount;
    private BigDecimal repairCostTotal;

    private Integer maintenanceCompletedCount;
    private Integer maintenancePlannedCount;
    private BigDecimal maintenanceRate; // %

    private Integer healthScore; // 0~100
    private Integer status; // 1=草稿,2=已生成,3=已导出

    /**
     * MinIO 对象名（objectName），形如：
     * health-reports/{moldId}/{periodStart}_{periodEnd}/{reportId}.pdf
     */
    private String pdfFilePath;

    private String generatedBy;
    private LocalDateTime generatedAt;
}


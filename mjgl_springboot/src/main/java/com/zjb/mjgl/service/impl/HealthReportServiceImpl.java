package com.zjb.mjgl.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.common.enums.RoleEnum;
import com.zjb.mjgl.mapper.HealthReportAnalyticsMapper;
import com.zjb.mjgl.mapper.HealthReportMapper;
import com.zjb.mjgl.mapper.MaintenancePlanMapper;
import com.zjb.mjgl.mapper.MoldsMapper;
import com.zjb.mjgl.pojo.dto.BatchIdsDTO;
import com.zjb.mjgl.pojo.dto.HealthReportGenerateParam;
import com.zjb.mjgl.pojo.dto.HealthReportQueryParam;
import com.zjb.mjgl.pojo.entity.HealthReports;
import com.zjb.mjgl.pojo.entity.MaintenancePlans;
import com.zjb.mjgl.pojo.entity.Molds;
import com.zjb.mjgl.pojo.vo.HealthFaultStatVO;
import com.zjb.mjgl.pojo.vo.HealthMaintenanceStatVO;
import com.zjb.mjgl.pojo.vo.HealthReportVO;
import com.zjb.mjgl.pojo.vo.HealthUsageStatVO;
import com.zjb.mjgl.pojo.vo.MoldMetaVO;
import com.zjb.mjgl.service.HealthReportService;
import com.zjb.mjgl.utils.HealthScoreCalculator;
import com.zjb.mjgl.utils.IdUtil;
import com.zjb.mjgl.utils.MinioUtil;
import com.zjb.mjgl.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthReportServiceImpl implements HealthReportService {

    private final HealthReportMapper healthReportMapper;
    private final HealthReportAnalyticsMapper healthReportAnalyticsMapper;
    private final MoldsMapper moldsMapper;
    private final MaintenancePlanMapper maintenancePlanMapper;
    private final MinioUtil minioUtil;

    @Override
    public Result<Map<String, Object>> generateReports(HealthReportGenerateParam param) {
        // 权限：默认仅管理员可触发生成/导出
        if (UserUtils.getCurrentUserDetails() == null) {
            return Result.fail("未登录用户无法生成健康报告");
        }
        if (UserUtils.getCurrentUserDetails().getRole() != RoleEnum.ADMIN) {
            return Result.fail("仅管理员可生成健康报告");
        }

        HealthReportGenerateParam effective = Optional.ofNullable(param).orElseGet(HealthReportGenerateParam::new);
        boolean exportPdf = Optional.ofNullable(effective.getExportPdf()).orElse(Boolean.TRUE);

        PeriodRange range = resolvePeriodRange(effective);
        LocalDate periodStart = range.start;
        LocalDate periodEnd = range.end;

        // 1) 取目标模具
        List<MoldMetaVO> metas;
        if (effective.getMoldId() != null && !effective.getMoldId().trim().isEmpty()) {
            Molds mold = moldsMapper.selectById(effective.getMoldId().trim());
            if (mold == null) {
                return Result.fail("未找到模具");
            }
            MoldMetaVO meta = new MoldMetaVO();
            meta.setId(mold.getId());
            meta.setMoldCode(mold.getMoldCode());
            meta.setName(mold.getName());
            meta.setCurrentStatus(mold.getCurrentStatus());
            metas = Collections.singletonList(meta);
        } else {
            metas = Optional.ofNullable(moldsMapper.listMeta()).orElseGet(Collections::emptyList);
        }

        List<String> moldIds = metas.stream()
                .map(MoldMetaVO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (moldIds.isEmpty()) {
            return Result.success(Collections.singletonMap("generatedCount", 0));
        }

        LocalDateTime startDt = periodStart.atStartOfDay();
        LocalDateTime endDt = periodEnd.atTime(LocalTime.of(23, 59, 59));

        // 2) 统计聚合数据（分表统计后在 Java 合并）
        Map<String, HealthUsageStatVO> usageMap = Optional.ofNullable(
                        healthReportAnalyticsMapper.listUsageStats(startDt, endDt, moldIds))
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(HealthUsageStatVO::getMoldId, Function.identity(), (a, b) -> a));

        Map<String, HealthFaultStatVO> faultMap = Optional.ofNullable(
                        healthReportAnalyticsMapper.listFaultStats(startDt, endDt, moldIds))
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(HealthFaultStatVO::getMoldId, Function.identity(), (a, b) -> a));

        Map<String, HealthMaintenanceStatVO> maintenanceMap = Optional.ofNullable(
                        healthReportAnalyticsMapper.listMaintenanceCompletedStats(startDt, endDt, moldIds))
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(HealthMaintenanceStatVO::getMoldId, Function.identity(), (a, b) -> a));

        // 3) 批量取保养计划模板（用于 plannedCount 推导）
        List<MaintenancePlans> plans = Optional.ofNullable(maintenancePlanMapper.listByMoldIds(moldIds))
                .orElseGet(Collections::emptyList);
        Map<String, List<MaintenancePlans>> plansByMoldId = plans.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(MaintenancePlans::getSpecificMoldId));

        // 4) 逐模具计算并写入 health_reports
        String operatorId = UserUtils.getCurrentUserId();
        LocalDateTime generatedAt = LocalDateTime.now();

        int skipCount = 0;
        int generatedCount = 0;
        int exportedCount = 0;
        int failedCount = 0;

        for (MoldMetaVO meta : metas) {
            if (meta == null || meta.getId() == null) {
                continue;
            }
            String moldId = meta.getId();

            HealthUsageStatVO uStat = usageMap.get(moldId);
            HealthFaultStatVO fStat = faultMap.get(moldId);
            HealthMaintenanceStatVO mStat = maintenanceMap.get(moldId);

            int U = Optional.ofNullable(uStat).map(HealthUsageStatVO::getTotalUsageCount).orElse(0);
            double T = Optional.ofNullable(uStat).map(HealthUsageStatVO::getTotalProductionTime)
                    .map(BigDecimal::doubleValue)
                    .orElse(0.0d);

            int F = Optional.ofNullable(fStat).map(HealthFaultStatVO::getFaultCount).orElse(0);
            double C = Optional.ofNullable(fStat).map(HealthFaultStatVO::getRepairCostTotal)
                    .map(BigDecimal::doubleValue)
                    .orElse(0.0d);

            int MC = Optional.ofNullable(mStat).map(HealthMaintenanceStatVO::getMaintenanceCompletedCount).orElse(0);

            List<MaintenancePlans> moldPlans = plansByMoldId.getOrDefault(moldId, Collections.emptyList());
            int MP = calcPlannedCount(moldPlans, periodStart, periodEnd, U);

            BigDecimal maintenanceRate = HealthScoreCalculator.safePercent(
                    BigDecimal.valueOf(MC), BigDecimal.valueOf(Math.max(MP, 1)))
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            maintenanceRate = clampMax100(maintenanceRate);

            int healthScore = HealthScoreCalculator.calcHealthScore(
                    U, T, F, C, MC, MP);

            HealthReports existing = healthReportMapper.selectByMoldIdAndPeriod(moldId, periodStart, periodEnd);
            try {
                if (existing != null && Integer.valueOf(3).equals(existing.getStatus())) {
                    skipCount++;
                    continue;
                }

                HealthReports report = Optional.ofNullable(existing).orElseGet(() -> {
                    HealthReports r = new HealthReports();
                    r.setId(IdUtil.fastUUID());
                    return r;
                });

                report.setMoldId(moldId);
                report.setReportTitle("模具健康评估报告");
                report.setReportPeriodStart(periodStart);
                report.setReportPeriodEnd(periodEnd);

                report.setTotalUsageCount(U);
                report.setTotalProductionTime(BigDecimal.valueOf(T).setScale(2, BigDecimal.ROUND_HALF_UP));
                report.setFaultCount(F);
                report.setRepairCostTotal(BigDecimal.valueOf(C).setScale(2, BigDecimal.ROUND_HALF_UP));

                report.setMaintenanceCompletedCount(MC);
                report.setMaintenancePlannedCount(MP);
                report.setMaintenanceRate(maintenanceRate);

                report.setHealthScore(healthScore);
                report.setStatus(2); // 已生成

                report.setPdfFilePath(null);
                report.setGeneratedBy(operatorId);
                report.setGeneratedAt(generatedAt);

                if (existing == null) {
                    healthReportMapper.insert(report);
                } else {
                    healthReportMapper.update(report);
                }

                generatedCount++;

                if (exportPdf) {
                    Result<String> r = exportPdf(report.getId());
                    if (r.getCode() == 200) {
                        exportedCount++;
                    } else {
                        failedCount++;
                    }
                }
            } catch (Exception e) {
                log.warn("生成健康报告失败, moldId={}, period={}-{}, err={}", moldId,
                        periodStart, periodEnd, e.getMessage());
                failedCount++;
            }
        }

        java.util.HashMap<String, Object> result = new java.util.HashMap<>();
        result.put("periodStart", periodStart.toString());
        result.put("periodEnd", periodEnd.toString());
        result.put("generatedCount", generatedCount);
        result.put("exportedCount", exportedCount);
        result.put("skipCount", skipCount);
        result.put("failedCount", failedCount);
        return Result.success(result);
    }

    @Override
    public Result<String> exportPdf(String reportId) {
        if (reportId == null || reportId.trim().isEmpty()) {
            return Result.fail("reportId不能为空");
        }
        HealthReports report = healthReportMapper.selectById(reportId.trim());
        if (report == null) {
            return Result.fail("未找到对应健康报告");
        }

        if (Integer.valueOf(3).equals(report.getStatus())
                && report.getPdfFilePath() != null
                && !report.getPdfFilePath().trim().isEmpty()) {
            return Result.success(report.getPdfFilePath());
        }

        Molds mold = moldsMapper.selectById(report.getMoldId());
        if (mold == null) {
            return Result.fail("未找到对应模具");
        }

        int U = Optional.ofNullable(report.getTotalUsageCount()).orElse(0);
        double T = Optional.ofNullable(report.getTotalProductionTime()).map(BigDecimal::doubleValue).orElse(0.0d);
        int F = Optional.ofNullable(report.getFaultCount()).orElse(0);
        double C = Optional.ofNullable(report.getRepairCostTotal()).map(BigDecimal::doubleValue).orElse(0.0d);

        int MC = Optional.ofNullable(report.getMaintenanceCompletedCount()).orElse(0);
        int MP = Optional.ofNullable(report.getMaintenancePlannedCount()).orElse(0);
        BigDecimal maintenanceRatePercent = report.getMaintenanceRate();
        if (maintenanceRatePercent == null) {
            maintenanceRatePercent = HealthScoreCalculator.safePercent(
                    BigDecimal.valueOf(MC), BigDecimal.valueOf(Math.max(MP, 1))).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        maintenanceRatePercent = clampMax100(maintenanceRatePercent);

        BigDecimal faultRate = BigDecimal.valueOf(F)
                .divide(BigDecimal.valueOf(Math.max(U, 1)), 6, BigDecimal.ROUND_HALF_UP);
        BigDecimal costRate = BigDecimal.valueOf(C)
                .divide(BigDecimal.valueOf(Math.max(T, 1.0d)), 6, BigDecimal.ROUND_HALF_UP);

        String riskLevel = HealthScoreCalculator.calcRiskLevel(Optional.ofNullable(report.getHealthScore()).orElse(0));
        List<String> actions = HealthScoreCalculator.recommendActions(
                Optional.ofNullable(report.getHealthScore()).orElse(0),
                maintenanceRatePercent,
                faultRate,
                costRate
        );

        String html = buildPdfHtml(report, mold, riskLevel, faultRate, costRate, actions);

        try {
            byte[] pdfBytes = renderHtmlToPdfBytes(html);

            String objectName = buildPdfObjectName(report);
            minioUtil.uploadBytes(pdfBytes, objectName, "application/pdf");

            report.setPdfFilePath(objectName);
            report.setStatus(3);
            report.setGeneratedAt(LocalDateTime.now());
            healthReportMapper.update(report);

            return Result.success(objectName);
        } catch (Exception e) {
            log.warn("健康报告 PDF 导出失败, reportId={}, err={}", reportId, e.getMessage());
            return Result.fail("PDF导出失败: " + e.getMessage());
        }
    }

    @Override
    public PageInfo<HealthReportVO> queryByCondition(HealthReportQueryParam param, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        HealthReportQueryParam effective = Optional.ofNullable(param).orElseGet(HealthReportQueryParam::new);
        List<HealthReportVO> list = Optional.ofNullable(healthReportMapper.queryByCondition(effective))
                .orElseGet(Collections::emptyList);

        // 补齐派生字段：riskLevel / recommendedActions / faultRate / costRate / pdfUrl
        list.forEach(vo -> {
            if (vo == null) return;
            Integer hs = vo.getHealthScore();
            vo.setRiskLevel(HealthScoreCalculator.calcRiskLevel(hs == null ? 0 : hs));

            int U = Optional.ofNullable(vo.getTotalUsageCount()).orElse(0);
            double T = Optional.ofNullable(vo.getTotalProductionTime()).map(BigDecimal::doubleValue).orElse(0.0d);
            int F = Optional.ofNullable(vo.getFaultCount()).orElse(0);
            double C = Optional.ofNullable(vo.getRepairCostTotal()).map(BigDecimal::doubleValue).orElse(0.0d);

            BigDecimal faultRate = BigDecimal.valueOf(F)
                    .divide(BigDecimal.valueOf(Math.max(U, 1)), 6, BigDecimal.ROUND_HALF_UP);
            BigDecimal costRate = BigDecimal.valueOf(C)
                    .divide(BigDecimal.valueOf(Math.max(T, 1.0d)), 6, BigDecimal.ROUND_HALF_UP);

            vo.setFaultRate(faultRate);
            vo.setCostRate(costRate);

            int MC = Optional.ofNullable(vo.getMaintenanceCompletedCount()).orElse(0);
            int MP = Optional.ofNullable(vo.getMaintenancePlannedCount()).orElse(0);
            BigDecimal maintenanceRatePercent = vo.getMaintenanceRate();
            if (maintenanceRatePercent == null) {
                maintenanceRatePercent = HealthScoreCalculator.safePercent(
                        BigDecimal.valueOf(MC), BigDecimal.valueOf(Math.max(MP, 1))).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
            maintenanceRatePercent = clampMax100(maintenanceRatePercent);

            vo.setRecommendedActions(HealthScoreCalculator.recommendActions(
                    vo.getHealthScore() == null ? 0 : vo.getHealthScore(),
                    maintenanceRatePercent,
                    faultRate,
                    costRate
            ));

            if (vo.getPdfFilePath() != null && !vo.getPdfFilePath().trim().isEmpty()) {
                vo.setPdfUrl(minioUtil.getObjectUrl(vo.getPdfFilePath(), 7));
            }
        });

        return new PageInfo<>(list);
    }

    @Override
    public Result<HealthReportVO> getById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Result.fail("id不能为空");
        }
        // V1 简化：先查询列表补齐 join，再用 id 过滤
        List<HealthReportVO> tmp = Optional.ofNullable(healthReportMapper.queryByCondition(new HealthReportQueryParam()))
                .orElseGet(Collections::emptyList);

        return tmp.stream()
                .filter(r -> r != null && id.equals(r.getId()))
                .findFirst()
                .map(vo -> {
                    // 补齐派生字段
                    Integer hs = vo.getHealthScore();
                    vo.setRiskLevel(HealthScoreCalculator.calcRiskLevel(hs == null ? 0 : hs));

                    int U = Optional.ofNullable(vo.getTotalUsageCount()).orElse(0);
                    double T = Optional.ofNullable(vo.getTotalProductionTime()).map(BigDecimal::doubleValue).orElse(0.0d);
                    int F = Optional.ofNullable(vo.getFaultCount()).orElse(0);
                    double C = Optional.ofNullable(vo.getRepairCostTotal()).map(BigDecimal::doubleValue).orElse(0.0d);

                    BigDecimal faultRate = BigDecimal.valueOf(F)
                            .divide(BigDecimal.valueOf(Math.max(U, 1)), 6, BigDecimal.ROUND_HALF_UP);
                    BigDecimal costRate = BigDecimal.valueOf(C)
                            .divide(BigDecimal.valueOf(Math.max(T, 1.0d)), 6, BigDecimal.ROUND_HALF_UP);
                    vo.setFaultRate(faultRate);
                    vo.setCostRate(costRate);

                    int MC = Optional.ofNullable(vo.getMaintenanceCompletedCount()).orElse(0);
                    int MP = Optional.ofNullable(vo.getMaintenancePlannedCount()).orElse(0);
                    BigDecimal maintenanceRatePercent = vo.getMaintenanceRate();
                    if (maintenanceRatePercent == null) {
                        maintenanceRatePercent = HealthScoreCalculator.safePercent(
                                BigDecimal.valueOf(MC), BigDecimal.valueOf(Math.max(MP, 1)))
                                .setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                    maintenanceRatePercent = clampMax100(maintenanceRatePercent);
                    vo.setRecommendedActions(HealthScoreCalculator.recommendActions(
                            vo.getHealthScore() == null ? 0 : vo.getHealthScore(),
                            maintenanceRatePercent,
                            faultRate,
                            costRate
                    ));

                    if (vo.getPdfFilePath() != null && !vo.getPdfFilePath().trim().isEmpty()) {
                        vo.setPdfUrl(minioUtil.getObjectUrl(vo.getPdfFilePath(), 7));
                    }

                    return Result.success(vo);
                })
                .orElseGet(() -> Result.fail("未找到该报告"));
    }

    @Override
    public Result<String> deleteById(String reportId) {
        if (reportId == null || reportId.trim().isEmpty()) {
            return Result.fail("reportId不能为空");
        }
        if (UserUtils.getCurrentUserDetails() == null) {
            return Result.fail("未登录用户无法删除健康报告");
        }
        if (UserUtils.getCurrentUserDetails().getRole() != RoleEnum.ADMIN) {
            return Result.fail("仅管理员可删除健康报告");
        }

        String id = reportId.trim();
        HealthReports report = healthReportMapper.selectById(id);
        if (report == null) {
            return Result.fail("未找到对应健康报告");
        }

        // best-effort：删除 PDF 失败不影响数据库删除
        String pdfFilePath = report.getPdfFilePath();
        if (pdfFilePath != null && !pdfFilePath.trim().isEmpty()) {
            try {
                minioUtil.deleteFile(pdfFilePath.trim());
            } catch (Exception e) {
                log.warn("删除健康报告 PDF 失败, reportId={}, pdfObjectName={}, err={}",
                        id, pdfFilePath, e.getMessage());
            }
        }

        int rows = healthReportMapper.deleteById(id);
        if (rows <= 0) {
            return Result.fail("删除失败：记录不存在或已被删除");
        }
        return Result.success("删除成功");
    }

    @Override
    public Result<String> deleteByIds(List<String> rawIds) {
        List<String> ids = BatchIdsDTO.normalizeList(rawIds);
        if (ids.isEmpty()) {
            return Result.fail("请选择要删除的报告");
        }
        return ids.stream()
                .map(this::deleteById)
                .filter(r -> r.getCode() != 200)
                .findFirst()
                .orElseGet(() -> Result.success("已删除 " + ids.size() + " 条健康报告"));
    }

    private PeriodRange resolvePeriodRange(HealthReportGenerateParam param) {
        if (param == null) {
            LocalDate today = LocalDate.now();
            return new PeriodRange(today.minusWeeks(1), today.minusDays(1));
        }

        if (param.getPeriodStart() != null && param.getPeriodEnd() != null) {
            return new PeriodRange(param.getPeriodStart(), param.getPeriodEnd());
        }

        String type = Optional.ofNullable(param.getPeriodType()).orElse("MONTHLY").trim().toUpperCase();
        LocalDate today = LocalDate.now();

        switch (type) {
            case "WEEKLY": {
                LocalDate start = today.minusWeeks(1);
                // 统一为周一~周日窗口
                int dow = start.getDayOfWeek().getValue(); // 1=Mon
                start = start.minusDays(dow - 1);
                LocalDate end = start.plusDays(6);
                return new PeriodRange(start, end);
            }
            case "QUARTERLY": {
                int currentQuarter = (today.getMonthValue() - 1) / 3; // 0~3
                int prevQuarter = currentQuarter - 1;
                int year = today.getYear();
                if (prevQuarter < 0) {
                    prevQuarter = 3;
                    year = year - 1;
                }
                int startMonth = prevQuarter * 3 + 1;
                LocalDate start = LocalDate.of(year, startMonth, 1);
                LocalDate end = start.plusMonths(3).minusDays(1);
                return new PeriodRange(start, end);
            }
            case "MONTHLY":
            default: {
                LocalDate end = today.minusDays(today.getDayOfMonth());
                LocalDate start = end.withDayOfMonth(1);
                return new PeriodRange(start, end);
            }
        }
    }

    private int calcPlannedCount(List<MaintenancePlans> plans,
                                 LocalDate periodStart,
                                 LocalDate periodEnd,
                                 int usageCount) {
        if (plans == null || plans.isEmpty()) return 0;
        return plans.stream()
                .filter(Objects::nonNull)
                .mapToInt(plan -> {
                    Integer scheduledDay = plan.getScheduledDayOfMonth();
                    Integer intervalHours = plan.getIntervalHours();
                    if (scheduledDay != null) {
                        return calcMonthlyDueCount(scheduledDay, periodStart, periodEnd);
                    }
                    if (intervalHours != null && intervalHours > 0) {
                        return usageCount / intervalHours;
                    }
                    return 0;
                })
                .sum();
    }

    private int calcMonthlyDueCount(int scheduledDay, LocalDate periodStart, LocalDate periodEnd) {
        LocalDate cursor = periodStart.withDayOfMonth(1);
        int planned = 0;
        while (!cursor.isAfter(periodEnd)) {
            int day = Math.min(scheduledDay, cursor.lengthOfMonth());
            LocalDate due = LocalDate.of(cursor.getYear(), cursor.getMonthValue(), day);
            if ((due.isEqual(periodStart) || due.isAfter(periodStart))
                    && (due.isEqual(periodEnd) || due.isBefore(periodEnd))) {
                planned++;
            }
            cursor = cursor.plusMonths(1);
        }
        return planned;
    }

    private String buildPdfObjectName(HealthReports report) {
        String safeStart = report.getReportPeriodStart() != null ? report.getReportPeriodStart().toString() : "null";
        String safeEnd = report.getReportPeriodEnd() != null ? report.getReportPeriodEnd().toString() : "null";
        return "health-reports/" + report.getMoldId() + "/" + safeStart + "_" + safeEnd + "/" + report.getId() + ".pdf";
    }

    private byte[] renderHtmlToPdfBytes(String html) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            // 强制加载中文字体并嵌入 PDF，避免缺字形导致 “####/乱码”
            configureChineseFont(builder);
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        }
    }

    private void configureChineseFont(PdfRendererBuilder builder) {
        // 注意：font-family 名称需要与下面 useFont(File, family, ...) 的 family 一致
        final String family = "MJGLFont";

        // Windows 常见中文字体路径（优先使用可显示中文的字体文件）
        String[] normalCandidates = {
                "C:\\Windows\\Fonts\\msyh.ttf",
                "C:\\Windows\\Fonts\\msyh.ttc",
                "C:\\Windows\\Fonts\\simhei.ttf",
                "C:\\Windows\\Fonts\\simsun.ttf",
                "C:\\Windows\\Fonts\\simsun.ttc"
        };
        String[] boldCandidates = {
                "C:\\Windows\\Fonts\\msyhbd.ttf",
                "C:\\Windows\\Fonts\\msyhbd.ttc"
        };

        File normal = java.util.Arrays.stream(normalCandidates)
                .map(File::new)
                .filter(File::exists)
                .findFirst()
                .orElse(null);

        File bold = java.util.Arrays.stream(boldCandidates)
                .map(File::new)
                .filter(File::exists)
                .findFirst()
                .orElse(null);

        try {
            if (normal != null) {
                builder.useFont(normal, family, 400, FontStyle.NORMAL, true);
            }
            if (bold != null) {
                builder.useFont(bold, family, 700, FontStyle.NORMAL, true);
            }
            // 如果没找到字体文件，就退回到 HTML/CSS 的默认字体回退（可能仍会乱码）
            if (normal == null && bold == null) {
                log.warn("未找到可用的中文字体文件，将依赖默认字体回退，可能导致 PDF 中文乱码");
            }
        } catch (Exception e) {
            log.warn("中文字体嵌入失败，可能导致 PDF 中文乱码: {}", e.getMessage());
        }
    }

    private String buildPdfHtml(HealthReports report,
                                 Molds mold,
                                 String riskLevel,
                                 BigDecimal faultRate,
                                 BigDecimal costRate,
                                 List<String> actions) {
        String title = escapeHtml(report.getReportTitle());
        String moldName = escapeHtml(mold.getName());
        String moldCode = escapeHtml(mold.getMoldCode());
        String periodStart = String.valueOf(report.getReportPeriodStart());
        String periodEnd = String.valueOf(report.getReportPeriodEnd());

        StringBuilder actionHtml = new StringBuilder();
        Optional.ofNullable(actions).orElse(Collections.emptyList())
                .forEach(a -> actionHtml.append("<li>").append(escapeHtml(a)).append("</li>"));

        return "<html><head><meta charset=\"utf-8\"/>" +
                "<style>" +
                "body{font-family:'MJGLFont','Microsoft YaHei','SimSun','Arial Unicode MS','Noto Sans CJK SC',Helvetica,sans-serif;" +
                "font-size:12px;color:#222;}" +
                "h1{font-size:18px;margin:0 0 10px 0;}" +
                "h2{font-size:14px;margin:14px 0 6px 0;}" +
                "table{width:100%;border-collapse:collapse;}" +
                "th,td{border:1px solid #ddd;padding:6px;vertical-align:top;}" +
                ".muted{color:#666;}" +
                "ul{margin:0;padding-left:18px;}" +
                "</style></head><body>" +
                "<h1>" + title + "</h1>" +
                "<div class='muted'>模具：" + moldCode + " / " + moldName + "</div>" +
                "<div class='muted'>统计周期：" + periodStart + " ~ " + periodEnd + "</div>" +
                "<h2>1. 核心指标</h2>" +
                "<table>" +
                "<tr><th>使用次数 U</th><td>" + nullSafe(report.getTotalUsageCount()) + "</td></tr>" +
                "<tr><th>生产时长 T（小时）</th><td>" + nullSafe(report.getTotalProductionTime()) + "</td></tr>" +
                "<tr><th>故障次数 F</th><td>" + nullSafe(report.getFaultCount()) + "</td></tr>" +
                "<tr><th>维修成本 C（元）</th><td>" + nullSafe(report.getRepairCostTotal()) + "</td></tr>" +
                "<tr><th>保养完成数 MC</th><td>" + nullSafe(report.getMaintenanceCompletedCount()) + "</td></tr>" +
                "<tr><th>保养计划数 MP</th><td>" + nullSafe(report.getMaintenancePlannedCount()) + "</td></tr>" +
                "<tr><th>保养完成率（%）</th><td>" + nullSafe(report.getMaintenanceRate()) + "</td></tr>" +
                "</table>" +
                "<h2>2. 综合健康评分</h2>" +
                "<table>" +
                "<tr><th>健康分（0~100）</th><td>" + nullSafe(report.getHealthScore()) + "</td></tr>" +
                "<tr><th>风险分级</th><td>" + escapeHtml(riskLevel) + "</td></tr>" +
                "<tr><th>故障率（F/U）</th><td>" + nullSafe(faultRate) + "</td></tr>" +
                "<tr><th>单位成本（C/T，元/小时）</th><td>" + nullSafe(costRate) + "</td></tr>" +
                "</table>" +
                "<h2>3. 管理层建议</h2>" +
                "<ul>" + actionHtml + "</ul>" +
                "<div class='muted' style='margin-top:10px;'>口径说明：本报告的指标口径用于健康评估与决策支持，默认基于系统记录的使用/维修/保养数据计算。</div>" +
                "</body></html>";
    }

    private String nullSafe(Object o) {
        return o == null ? "0" : String.valueOf(o);
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private BigDecimal clampMax100(BigDecimal v) {
        if (v == null) return BigDecimal.ZERO;
        BigDecimal max = new BigDecimal("100");
        if (v.compareTo(max) > 0) return max;
        if (v.compareTo(BigDecimal.ZERO) < 0) return BigDecimal.ZERO;
        return v;
    }

    private static class PeriodRange {
        private final LocalDate start;
        private final LocalDate end;

        private PeriodRange(LocalDate start, LocalDate end) {
            this.start = start;
            this.end = end;
        }
    }
}


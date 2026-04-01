package com.zjb.mjgl.service.impl;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.mapper.HealthReportAnalyticsMapper;
import com.zjb.mjgl.mapper.MaintenancePlanMapper;
import com.zjb.mjgl.mapper.MoldStatisticsAnalyticsMapper;
import com.zjb.mjgl.mapper.MoldsMapper;
import com.zjb.mjgl.pojo.dto.MoldStatisticsQueryParam;
import com.zjb.mjgl.pojo.dto.MoldTrendsQueryParam;
import com.zjb.mjgl.pojo.entity.MaintenancePlans;
import com.zjb.mjgl.pojo.vo.HealthMaintenanceStatVO;
import com.zjb.mjgl.pojo.vo.HealthUsageStatVO;
import com.zjb.mjgl.pojo.vo.MoldMetaVO;
import com.zjb.mjgl.pojo.vo.MoldRepairDurationStatVO;
import com.zjb.mjgl.pojo.vo.MoldStatVO;
import com.zjb.mjgl.pojo.vo.MoldMaintenanceTrendPointVO;
import com.zjb.mjgl.pojo.vo.MoldRepairTrendPointVO;
import com.zjb.mjgl.pojo.vo.MoldTrendsResponseVO;
import com.zjb.mjgl.pojo.vo.MoldUsageTrendPointVO;
import com.zjb.mjgl.service.MoldStatisticsService;
import com.zjb.mjgl.utils.HealthScoreCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoldStatisticsServiceImpl implements MoldStatisticsService {

    private final MoldsMapper moldsMapper;
    private final HealthReportAnalyticsMapper healthReportAnalyticsMapper;
    private final MaintenancePlanMapper maintenancePlanMapper;
    private final MoldStatisticsAnalyticsMapper moldStatisticsAnalyticsMapper;

    @Override
    public Result<List<MoldStatVO>> queryMoldStats(MoldStatisticsQueryParam param) {
        MoldStatisticsQueryParam effective = Optional.ofNullable(param).orElse(new MoldStatisticsQueryParam());

        LocalDate endDate0 = Optional.ofNullable(effective.getEndDate()).orElse(LocalDate.now());
        LocalDate startDate0 = Optional.ofNullable(effective.getStartDate()).orElse(endDate0.minusDays(29));
        boolean swap = startDate0.isAfter(endDate0);
        LocalDate startDate = swap ? endDate0 : startDate0;
        LocalDate endDate = swap ? startDate0 : endDate0;

        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt = endDate.atTime(LocalTime.of(23, 59, 59));

        String moldIdFilter = normalizeId(effective.getMoldId());

        List<MoldMetaVO> metas = Optional.ofNullable(moldsMapper.listMeta()).orElseGet(Collections::emptyList);
        if (moldIdFilter != null) {
            metas = metas.stream()
                    .filter(m -> moldIdFilter.equals(m.getId()))
                    .collect(Collectors.toList());
        }

        List<String> moldIds = metas.stream()
                .map(MoldMetaVO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (moldIds.isEmpty()) {
            return Result.success(Collections.emptyList());
        }

        List<HealthUsageStatVO> usageStats = Optional
                .ofNullable(healthReportAnalyticsMapper.listUsageStats(startDt, endDt, moldIds))
                .orElseGet(Collections::emptyList);

        Map<String, HealthUsageStatVO> usageMap = usageStats.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(HealthUsageStatVO::getMoldId, v -> v, (a, b) -> a));

        List<HealthMaintenanceStatVO> maintenanceStats = Optional
                .ofNullable(healthReportAnalyticsMapper.listMaintenanceCompletedStats(startDt, endDt, moldIds))
                .orElseGet(Collections::emptyList);

        Map<String, HealthMaintenanceStatVO> maintenanceMap = maintenanceStats.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(HealthMaintenanceStatVO::getMoldId, v -> v, (a, b) -> a));

        List<MoldRepairDurationStatVO> repairStats = Optional
                .ofNullable(moldStatisticsAnalyticsMapper.listRepairDurationStats(startDt, endDt, moldIds))
                .orElseGet(Collections::emptyList);

        Map<String, MoldRepairDurationStatVO> repairMap = repairStats.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(MoldRepairDurationStatVO::getMoldId, v -> v, (a, b) -> a));

        // 取保养计划模板，用于推导“计划数 MP”（口径复用健康报告的 plannedCount 推导）
        List<MaintenancePlans> plans = Optional
                .ofNullable(maintenancePlanMapper.listByMoldIds(moldIds))
                .orElseGet(Collections::emptyList);
        Map<String, List<MaintenancePlans>> plansByMoldId = plans.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(MaintenancePlans::getSpecificMoldId));

        // 组装结果
        List<MoldStatVO> result = metas.stream()
                .filter(Objects::nonNull)
                .map(meta -> {
                    String mid = meta.getId();

                    HealthUsageStatVO u = usageMap.get(mid);
                    int U = Optional.ofNullable(u).map(HealthUsageStatVO::getTotalUsageCount).orElse(0);
                    BigDecimal T = Optional.ofNullable(u).map(HealthUsageStatVO::getTotalProductionTime).orElse(BigDecimal.ZERO);

                    MoldRepairDurationStatVO r = repairMap.get(mid);
                    int repairFreq = Optional.ofNullable(r).map(MoldRepairDurationStatVO::getRepairFrequency).orElse(0);
                    BigDecimal avgRepairHours = Optional.ofNullable(r).map(MoldRepairDurationStatVO::getAvgRepairDurationHours).orElse(BigDecimal.ZERO);

                    HealthMaintenanceStatVO m = maintenanceMap.get(mid);
                    int MC = Optional.ofNullable(m).map(HealthMaintenanceStatVO::getMaintenanceCompletedCount).orElse(0);

                    List<MaintenancePlans> moldPlans = plansByMoldId.getOrDefault(mid, Collections.emptyList());
                    int MP = calcPlannedCount(moldPlans, startDate, endDate, U);

                    BigDecimal maintenanceRate = HealthScoreCalculator.safePercent(
                            BigDecimal.valueOf(MC),
                            BigDecimal.valueOf(Math.max(MP, 1))
                    ).setScale(2, RoundingMode.HALF_UP);
                    maintenanceRate = clampMax100(maintenanceRate);

                    MoldStatVO vo = new MoldStatVO();
                    vo.setMoldId(mid);
                    vo.setMoldCode(meta.getMoldCode());
                    vo.setMoldName(meta.getName());
                    vo.setTotalUsageCount(U);
                    vo.setTotalProductionTimeHours(T);
                    vo.setRepairFrequency(repairFreq);
                    vo.setAvgRepairDurationHours(avgRepairHours);
                    vo.setMaintenancePlannedCount(MP);
                    vo.setMaintenanceCompletedCount(MC);
                    vo.setMaintenanceRatePercent(maintenanceRate);
                    return vo;
                })
                .sorted(Comparator.comparing(MoldStatVO::getRepairFrequency, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(MoldStatVO::getTotalUsageCount, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        return Result.success(result);
    }

    @Override
    public Result<MoldTrendsResponseVO> queryTrends(MoldTrendsQueryParam param) {
        MoldTrendsQueryParam effective = Optional.ofNullable(param).orElse(new MoldTrendsQueryParam());

        LocalDate endDate = Optional.ofNullable(effective.getEndDate()).orElse(LocalDate.now());
        LocalDate startDate = Optional.ofNullable(effective.getStartDate()).orElse(endDate.minusDays(29));
        if (startDate.isAfter(endDate)) {
            LocalDate tmp = startDate;
            startDate = endDate;
            endDate = tmp;
        }

        LocalDateTime startDt = startDate.atStartOfDay();
        LocalDateTime endDt = endDate.atTime(LocalTime.of(23, 59, 59));

        String bucketType = Optional.ofNullable(effective.getBucketType())
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .orElse("WEEK");

        String moldId = normalizeId(effective.getMoldId());

        List<MoldUsageTrendPointVO> usage = Optional.ofNullable(
                        moldStatisticsAnalyticsMapper.listUsageTrends(startDt, endDt, moldId, bucketType))
                .orElseGet(Collections::emptyList);
        List<MoldRepairTrendPointVO> repairs = Optional.ofNullable(
                        moldStatisticsAnalyticsMapper.listRepairTrends(startDt, endDt, moldId, bucketType))
                .orElseGet(Collections::emptyList);
        List<MoldMaintenanceTrendPointVO> maint = Optional.ofNullable(
                        moldStatisticsAnalyticsMapper.listMaintenanceTrends(startDt, endDt, moldId, bucketType))
                .orElseGet(Collections::emptyList);

        Map<String, MoldUsageTrendPointVO> usageMap = usage.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(MoldUsageTrendPointVO::getBucketKey, v -> v, (a, b) -> a));
        Map<String, MoldRepairTrendPointVO> repairMap = repairs.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(MoldRepairTrendPointVO::getBucketKey, v -> v, (a, b) -> a));
        Map<String, MoldMaintenanceTrendPointVO> maintMap = maint.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(MoldMaintenanceTrendPointVO::getBucketKey, v -> v, (a, b) -> a));

        Set<String> bucketSet = new TreeSet<>();
        usage.forEach(p -> Optional.ofNullable(p).map(MoldUsageTrendPointVO::getBucketKey).ifPresent(bucketSet::add));
        repairs.forEach(p -> Optional.ofNullable(p).map(MoldRepairTrendPointVO::getBucketKey).ifPresent(bucketSet::add));
        maint.forEach(p -> Optional.ofNullable(p).map(MoldMaintenanceTrendPointVO::getBucketKey).ifPresent(bucketSet::add));

        List<String> labels = new ArrayList<>(bucketSet);

        MoldTrendsResponseVO resp = new MoldTrendsResponseVO();
        resp.setLabels(labels);

        List<Integer> usageCounts = labels.stream()
                .map(k -> Optional.ofNullable(usageMap.get(k)).map(MoldUsageTrendPointVO::getUsageCount).orElse(0))
                .collect(Collectors.toList());
        List<Double> usageProductionHours = labels.stream()
                .map(k -> Optional.ofNullable(usageMap.get(k)).map(MoldUsageTrendPointVO::getProductionHours).map(BigDecimal::doubleValue).orElse(0.0d))
                .collect(Collectors.toList());

        List<Integer> repairCounts = labels.stream()
                .map(k -> Optional.ofNullable(repairMap.get(k)).map(MoldRepairTrendPointVO::getRepairCount).orElse(0))
                .collect(Collectors.toList());
        List<Double> avgRepairDurationHours = labels.stream()
                .map(k -> Optional.ofNullable(repairMap.get(k)).map(MoldRepairTrendPointVO::getAvgRepairDurationHours).map(BigDecimal::doubleValue).orElse(0.0d))
                .collect(Collectors.toList());

        List<Integer> maintenanceCounts = labels.stream()
                .map(k -> Optional.ofNullable(maintMap.get(k)).map(MoldMaintenanceTrendPointVO::getMaintenanceCount).orElse(0))
                .collect(Collectors.toList());

        resp.setUsageCounts(usageCounts);
        resp.setUsageProductionHours(usageProductionHours);
        resp.setRepairCounts(repairCounts);
        resp.setAvgRepairDurationHours(avgRepairDurationHours);
        resp.setMaintenanceCounts(maintenanceCounts);

        return Result.success(resp);
    }

    @Override
    public ResponseEntity<byte[]> exportMoldStatsCsv(MoldStatisticsQueryParam param) {
        List<MoldStatVO> stats = queryMoldStats(param).getData();

        MoldStatisticsQueryParam effective = Optional.ofNullable(param).orElse(new MoldStatisticsQueryParam());
        LocalDate endDate = Optional.ofNullable(effective.getEndDate()).orElse(LocalDate.now());
        LocalDate startDate = Optional.ofNullable(effective.getStartDate()).orElse(endDate.minusDays(29));
        if (startDate.isAfter(endDate)) {
            LocalDate tmp = startDate;
            startDate = endDate;
            endDate = tmp;
        }

        String fileName = "模具统计报表_使用-维修-保养_统计窗口_" + startDate + "至" + endDate + ".csv";

        byte[] csvBytes = buildCsvBytes(stats);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=utf-8"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }

    private byte[] buildCsvBytes(List<MoldStatVO> stats) {
        String[] header = new String[]{
                "模具编号",
                "模具名称",
                "累计使用次数(次)",
                "累计生产时长(小时)",
                "维修次数(次)",
                "平均维修时长(小时)",
                "保养计划数(推导MP, 次)",
                "保养完成数(次)",
                "保养周期达标率(%)"
        };

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {

            // BOM：Excel 友好
            writer.write("\uFEFF");
            writer.write(String.join(",", header));
            writer.write("\n");

            Optional.ofNullable(stats).orElseGet(Collections::emptyList)
                    .forEach(s -> {
                        try {
                            List<String> cells = new ArrayList<>();
                            cells.add(csvCell(s.getMoldCode()));
                            cells.add(csvCell(s.getMoldName()));
                            cells.add(String.valueOf(Optional.ofNullable(s.getTotalUsageCount()).orElse(0)));
                            cells.add(String.valueOf(Optional.ofNullable(s.getTotalProductionTimeHours()).orElse(BigDecimal.ZERO).doubleValue()));
                            cells.add(String.valueOf(Optional.ofNullable(s.getRepairFrequency()).orElse(0)));
                            cells.add(String.valueOf(Optional.ofNullable(s.getAvgRepairDurationHours()).orElse(BigDecimal.ZERO).doubleValue()));
                            cells.add(String.valueOf(Optional.ofNullable(s.getMaintenancePlannedCount()).orElse(0)));
                            cells.add(String.valueOf(Optional.ofNullable(s.getMaintenanceCompletedCount()).orElse(0)));
                            cells.add(String.valueOf(Optional.ofNullable(s.getMaintenanceRatePercent()).orElse(BigDecimal.ZERO).doubleValue()));
                            writer.write(String.join(",", cells));
                            writer.write("\n");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

            writer.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            log.warn("CSV 导出失败: {}", e.getMessage());
            return new byte[0];
        }
    }

    @Override
    public ResponseEntity<byte[]> exportMoldStatsXlsx(MoldStatisticsQueryParam param) {
        List<MoldStatVO> stats = queryMoldStats(param).getData();

        MoldStatisticsQueryParam effective = Optional.ofNullable(param).orElse(new MoldStatisticsQueryParam());
        LocalDate endDate = Optional.ofNullable(effective.getEndDate()).orElse(LocalDate.now());
        LocalDate startDate = Optional.ofNullable(effective.getStartDate()).orElse(endDate.minusDays(29));
        if (startDate.isAfter(endDate)) {
            LocalDate tmp = startDate;
            startDate = endDate;
            endDate = tmp;
        }

        String fileName = "模具统计报表_使用-维修-保养_统计窗口_" + startDate + "至" + endDate + ".xlsx";

        byte[] xlsxBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("模具统计报表");

            // 表头
            Row headerRow = sheet.createRow(0);
            String[] header = new String[]{
                    "模具编号",
                    "模具名称",
                    "累计使用次数(次)",
                    "累计生产时长(小时)",
                    "维修次数(次)",
                    "平均维修时长(小时)",
                    "保养计划数(推导MP, 次)",
                    "保养完成数(次)",
                    "保养周期达标率(%)"
            };

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            for (int i = 0; i < header.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(header[i]);
                cell.setCellStyle(headerStyle);
            }

            // 列宽（单位：字符宽 * 256）。中文/英文混排按经验给一个偏宽的值。
            int[] colWidths = new int[]{
                    16, 18, 18, 20, 14, 18, 20, 18, 20
            };
            for (int i = 0; i < colWidths.length; i++) {
                sheet.setColumnWidth(i, colWidths[i] * 256);
            }

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.LEFT);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            for (int r = 0; r < Optional.ofNullable(stats).orElseGet(Collections::emptyList).size(); r++) {
                MoldStatVO s = stats.get(r);
                Row row = sheet.createRow(r + 1);

                row.createCell(0).setCellValue(nvlStr(s.getMoldCode()));
                row.createCell(1).setCellValue(nvlStr(s.getMoldName()));

                row.createCell(2).setCellValue(nvlInt(s.getTotalUsageCount()));
                row.createCell(3).setCellValue(nvlBigDouble(s.getTotalProductionTimeHours()));

                row.createCell(4).setCellValue(nvlInt(s.getRepairFrequency()));
                row.createCell(5).setCellValue(nvlBigDouble(s.getAvgRepairDurationHours()));

                row.createCell(6).setCellValue(nvlInt(s.getMaintenancePlannedCount()));
                row.createCell(7).setCellValue(nvlInt(s.getMaintenanceCompletedCount()));

                row.createCell(8).setCellValue(nvlBigDouble(s.getMaintenanceRatePercent()));

                // 应用统一样式
                for (int c = 0; c < header.length; c++) {
                    row.getCell(c).setCellStyle(cellStyle);
                }
            }

            workbook.setActiveSheet(0);

            // 必须写入输出流，否则 baos 为空，导出的 xlsx 会“无内容”
            workbook.write(baos);
            xlsxBytes = baos.toByteArray();
        } catch (Exception e) {
            log.warn("XLSX 导出失败: {}", e.getMessage());
            xlsxBytes = new byte[0];
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        ));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        return ResponseEntity.ok()
                .headers(headers)
                .body(xlsxBytes);
    }

    private static String nvlStr(String s) {
        return s == null ? "" : s;
    }

    private static int nvlInt(Integer v) {
        return v == null ? 0 : v;
    }

    private static double nvlBigDouble(BigDecimal v) {
        return v == null ? 0.0d : v.doubleValue();
    }

    private String csvCell(String s) {
        String v = s == null ? "" : s;
        boolean needsQuote = v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r");
        if (!needsQuote) return v;
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }

    private static String normalizeId(String id) {
        if (id == null) return null;
        String t = id.trim();
        return t.isEmpty() ? null : t;
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

    private BigDecimal clampMax100(BigDecimal v) {
        if (v == null) return BigDecimal.ZERO;
        BigDecimal max = new BigDecimal("100");
        if (v.compareTo(max) > 0) return max;
        if (v.compareTo(BigDecimal.ZERO) < 0) return BigDecimal.ZERO;
        return v;
    }
}


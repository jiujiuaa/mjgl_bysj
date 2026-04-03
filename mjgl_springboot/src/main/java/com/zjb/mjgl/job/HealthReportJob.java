package com.zjb.mjgl.job;

import com.zjb.mjgl.pojo.dto.HealthReportGenerateParam;
import com.zjb.mjgl.service.HealthReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealthReportJob {

    private final HealthReportService healthReportService;

    /**
     * 每日执行一次，根据日期判断是否需要生成周/月/季度报表。
     */
    /** 由 {@link BusinessJobScheduler} 按配置 Cron 调用 */
    public void runDaily() {
        LocalDate today = LocalDate.now();
        boolean exportPdf = true;

        try {
            // 周报：当日为周一 -> 生成上一周（周一~周日）
            if (today.getDayOfWeek().getValue() == 1) {
                LocalDate start = today.minusWeeks(1);
                LocalDate end = today.minusDays(1);
                runOne("WEEKLY", start, end, exportPdf);
            }

            // 月报：当日为每月 1 日 -> 生成上一月
            if (today.getDayOfMonth() == 1) {
                LocalDate end = today.minusDays(1);
                LocalDate start = end.withDayOfMonth(1);
                runOne("MONTHLY", start, end, exportPdf);

                // 季度报：上一季度为 1/4/7/10 月份的上一段
                int m = today.getMonthValue();
                if (m == 1 || m == 4 || m == 7 || m == 10) {
                    int prevQuarterMonthStart = m - 3;
                    int year = today.getYear();
                    if (prevQuarterMonthStart <= 0) {
                        prevQuarterMonthStart += 12;
                        year -= 1;
                    }
                    LocalDate qStart = LocalDate.of(year, prevQuarterMonthStart, 1);
                    LocalDate qEnd = qStart.plusMonths(3).minusDays(1);
                    runOne("QUARTERLY", qStart, qEnd, exportPdf);
                }
            }
        } catch (Exception e) {
            log.warn("健康报告定时任务执行失败: {}", e.getMessage());
        }
    }

    private void runOne(String type, LocalDate start, LocalDate end, boolean exportPdf) {
        try {
            HealthReportGenerateParam param = new HealthReportGenerateParam();
            param.setPeriodType(type);
            param.setPeriodStart(start);
            param.setPeriodEnd(end);
            param.setExportPdf(exportPdf);
            log.info("开始生成健康报告: type={}, period={}~{}", type, start, end);
            healthReportService.generateReports(param);
            log.info("健康报告生成结束: type={}, period={}~{}", type, start, end);
        } catch (Exception e) {
            log.warn("生成健康报告失败: type={}, period={}~{}, err={}", type, start, end, e.getMessage());
        }
    }
}


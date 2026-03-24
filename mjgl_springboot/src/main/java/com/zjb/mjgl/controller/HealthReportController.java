package com.zjb.mjgl.controller;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.BatchIdsDTO;
import com.zjb.mjgl.pojo.dto.HealthReportGenerateParam;
import com.zjb.mjgl.pojo.dto.HealthReportQueryParam;
import com.zjb.mjgl.pojo.vo.HealthReportVO;
import com.zjb.mjgl.service.HealthReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/health-reports")
@RequiredArgsConstructor
public class HealthReportController {

    private final HealthReportService healthReportService;

    /**
     * 触发生成周期性健康报告（默认导出 PDF 并归档）。
     */
    @PostMapping("/generate")
    public Result<Map<String, Object>> generate(@RequestBody(required = false) HealthReportGenerateParam param) {
        return healthReportService.generateReports(param);
    }

    /**
     * 导出指定健康报告 PDF 并归档（更新 status=3）。
     */
    @PostMapping("/{id}/export-pdf")
    public Result<String> exportPdf(@PathVariable("id") String id) {
        return healthReportService.exportPdf(id);
    }

    /**
     * 分页查询健康报告列表。
     */
    @PostMapping("/query")
    public Result<PageInfo<HealthReportVO>> query(
            @RequestBody(required = false) HealthReportQueryParam param,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        HealthReportQueryParam effective = param != null ? param : new HealthReportQueryParam();
        return Result.success(healthReportService.queryByCondition(effective, pageNum, pageSize));
    }

    /**
     * 根据 id 查询单条报告。
     */
    @GetMapping("/{id}")
    public Result<HealthReportVO> getById(@PathVariable("id") String id) {
        return healthReportService.getById(id);
    }

    /**
     * 删除指定报告（同时删除 PDF 归档文件）。
     */
    @PostMapping("/{id}/delete")
    public Result<String> deleteById(@PathVariable("id") String id) {
        return healthReportService.deleteById(id);
    }

    @PostMapping("/batch-delete")
    public Result<String> batchDelete(@RequestBody BatchIdsDTO body) {
        return healthReportService.deleteByIds(body == null ? null : body.getIds());
    }
}


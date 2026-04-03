package com.zjb.mjgl.controller;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.BatchIdsDTO;
import com.zjb.mjgl.pojo.dto.HealthReportGenerateParam;
import com.zjb.mjgl.pojo.dto.HealthReportQueryParam;
import com.zjb.mjgl.pojo.vo.HealthReportVO;
import com.zjb.mjgl.service.HealthReportService;
import com.zjb.mjgl.web.DynamicPageSize;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/health-reports")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class HealthReportController {

    private final HealthReportService healthReportService;

    /**
     * 触发生成周期性健康报告（默认导出 PDF 并归档）。
     */
    @PostMapping("/generate")
    public Result<Map<String, Object>> generate(@RequestBody(required = false) HealthReportGenerateParam param) {
        try {
            return healthReportService.generateReports(param);
        } catch (Exception e) {
            log.error("生成健康报告异常", e);
            return Result.fail("生成健康报告失败: " + e.getMessage());
        }
    }

    /**
     * 导出指定健康报告 PDF 并归档（更新 status=3）。
     */
    @PostMapping("/{id}/export-pdf")
    public Result<String> exportPdf(@PathVariable("id") String id) {
        try {
            return healthReportService.exportPdf(id);
        } catch (Exception e) {
            log.error("导出健康报告PDF异常, id={}", id, e);
            return Result.fail("导出PDF失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询健康报告列表。
     */
    @PostMapping("/query")
    public Result<PageInfo<HealthReportVO>> query(
            @RequestBody(required = false) HealthReportQueryParam param,
            @RequestParam(defaultValue = "1") int pageNum,
            @DynamicPageSize int pageSize) {
        try {
            HealthReportQueryParam effective = param != null ? param : new HealthReportQueryParam();
            return Result.success(healthReportService.queryByCondition(effective, pageNum, pageSize));
        } catch (Exception e) {
            log.error("查询健康报告异常, pageNum={}, pageSize={}", pageNum, pageSize, e);
            return Result.fail("查询健康报告失败: " + e.getMessage());
        }
    }

    /**
     * 根据 id 查询单条报告。
     */
    @GetMapping("/{id}")
    public Result<HealthReportVO> getById(@PathVariable("id") String id) {
        try {
            return healthReportService.getById(id);
        } catch (Exception e) {
            log.error("查询健康报告详情异常, id={}", id, e);
            return Result.fail("查询健康报告详情失败: " + e.getMessage());
        }
    }

    /**
     * 删除指定报告（同时删除 PDF 归档文件）。
     */
    @PostMapping("/{id}/delete")
    public Result<String> deleteById(@PathVariable("id") String id) {
        try {
            return healthReportService.deleteById(id);
        } catch (Exception e) {
            log.error("删除健康报告异常, id={}", id, e);
            return Result.fail("删除健康报告失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch-delete")
    public Result<String> batchDelete(@RequestBody BatchIdsDTO body) {
        try {
            return healthReportService.deleteByIds(body == null ? null : body.getIds());
        } catch (Exception e) {
            log.error("批量删除健康报告异常", e);
            return Result.fail("批量删除健康报告失败: " + e.getMessage());
        }
    }
}


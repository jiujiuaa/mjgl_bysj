package com.zjb.mjgl.controller;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.MoldStatisticsQueryParam;
import com.zjb.mjgl.pojo.dto.MoldTrendsQueryParam;
import com.zjb.mjgl.pojo.vo.MoldStatVO;
import com.zjb.mjgl.pojo.vo.MoldTrendsResponseVO;
import com.zjb.mjgl.service.MoldStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mold-statistics")
@PreAuthorize("isAuthenticated()")
public class MoldStatisticsController {

    private final MoldStatisticsService moldStatisticsService;

    /**
     * 模具维度统计：累计使用次数、累计生产时长、维修频率、平均维修时长、保养周期达标率
     */
    @PostMapping("/mold-stats")
    public Result<List<MoldStatVO>> moldStats(@RequestBody(required = false) MoldStatisticsQueryParam param) {
        try {
            return moldStatisticsService.queryMoldStats(param);
        } catch (Exception e) {
            log.error("查询模具统计异常", e);
            return Result.fail("查询模具统计失败: " + e.getMessage());
        }
    }

    /**
     * 维修/保养/使用趋势（按时间桶聚合）
     */
    @PostMapping("/trends")
    public Result<MoldTrendsResponseVO> trends(@RequestBody(required = false) MoldTrendsQueryParam param) {
        try {
            return moldStatisticsService.queryTrends(param);
        } catch (Exception e) {
            log.error("查询趋势统计异常", e);
            return Result.fail("查询趋势统计失败: " + e.getMessage());
        }
    }

    /**
     * CSV 导出模具统计报表（Excel 可直接打开）
     */
    @PostMapping(value = "/export/mold-stats", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<byte[]> exportMoldStatsCsv(@RequestBody(required = false) MoldStatisticsQueryParam param) {
        try {
            return moldStatisticsService.exportMoldStatsCsv(param);
        } catch (Exception e) {
            log.error("导出模具统计CSV异常", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 导出模具统计报表（XLSX，Excel 可自动识别并支持列宽控制）
     */
    @PostMapping(value = "/export/mold-stats-xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportMoldStatsXlsx(@RequestBody(required = false) MoldStatisticsQueryParam param) {
        try {
            return moldStatisticsService.exportMoldStatsXlsx(param);
        } catch (Exception e) {
            log.error("导出模具统计XLSX异常", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}


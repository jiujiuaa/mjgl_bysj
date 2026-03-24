package com.zjb.mjgl.service;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.HealthReportGenerateParam;
import com.zjb.mjgl.pojo.dto.HealthReportQueryParam;
import com.zjb.mjgl.pojo.vo.HealthReportVO;

import java.util.List;
import java.util.Map;

public interface HealthReportService {

    /**
     * 生成周期性健康报告，并可选同步导出 PDF。
     */
    Result<Map<String, Object>> generateReports(HealthReportGenerateParam param);

    /**
     * 导出指定报告 PDF 并更新归档状态。
     */
    Result<String> exportPdf(String reportId);

    /**
     * 查询报告列表（分页）。
     */
    PageInfo<HealthReportVO> queryByCondition(HealthReportQueryParam param, int pageNum, int pageSize);

    /**
     * 查询单条报告。
     */
    Result<HealthReportVO> getById(String id);

    /**
     * 删除指定报告（如存在 PDF 则同步删除 MinIO 对象）。
     */
    Result<String> deleteById(String reportId);

    Result<String> deleteByIds(List<String> reportIds);
}


package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.vo.HealthFaultStatVO;
import com.zjb.mjgl.pojo.vo.HealthMaintenanceStatVO;
import com.zjb.mjgl.pojo.vo.HealthUsageStatVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HealthReportAnalyticsMapper {

    /**
     * 统计使用次数与生产时长（小时）
     */
    List<HealthUsageStatVO> listUsageStats(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end,
                                             @Param("moldIds") List<String> moldIds);

    /**
     * 统计故障次数与维修成本总额
     */
    List<HealthFaultStatVO> listFaultStats(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end,
                                             @Param("moldIds") List<String> moldIds);

    /**
     * 统计保养完成次数
     */
    List<HealthMaintenanceStatVO> listMaintenanceCompletedStats(@Param("start") LocalDateTime start,
                                                                    @Param("end") LocalDateTime end,
                                                                    @Param("moldIds") List<String> moldIds);
}


package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.vo.MoldMaintenanceTrendPointVO;
import com.zjb.mjgl.pojo.vo.MoldRepairDurationStatVO;
import com.zjb.mjgl.pojo.vo.MoldRepairTrendPointVO;
import com.zjb.mjgl.pojo.vo.MoldUsageTrendPointVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MoldStatisticsAnalyticsMapper {

    /**
     * 维修频率 + 平均维修时长（小时）
     */
    List<MoldRepairDurationStatVO> listRepairDurationStats(@Param("start") LocalDateTime start,
                                                            @Param("end") LocalDateTime end,
                                                            @Param("moldIds") List<String> moldIds);

    /**
     * 使用/生产趋势：按实际结束时间桶聚合
     */
    List<MoldUsageTrendPointVO> listUsageTrends(@Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end,
                                                 @Param("moldId") String moldId,
                                                 @Param("bucketType") String bucketType);

    /**
     * 维修趋势：按维修开始时间桶聚合
     */
    List<MoldRepairTrendPointVO> listRepairTrends(@Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end,
                                                   @Param("moldId") String moldId,
                                                   @Param("bucketType") String bucketType);

    /**
     * 保养趋势：按实际结束时间/创建时间桶聚合
     */
    List<MoldMaintenanceTrendPointVO> listMaintenanceTrends(@Param("start") LocalDateTime start,
                                                              @Param("end") LocalDateTime end,
                                                              @Param("moldId") String moldId,
                                                              @Param("bucketType") String bucketType);
}


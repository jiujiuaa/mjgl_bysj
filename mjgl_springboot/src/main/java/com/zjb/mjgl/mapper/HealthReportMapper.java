package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.dto.HealthReportQueryParam;
import com.zjb.mjgl.pojo.entity.HealthReports;
import com.zjb.mjgl.pojo.vo.HealthReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface HealthReportMapper {

    int insert(HealthReports entity);

    int update(HealthReports entity);

    HealthReports selectById(@Param("id") String id);

    HealthReports selectByMoldIdAndPeriod(@Param("moldId") String moldId,
                                          @Param("periodStart") LocalDate periodStart,
                                          @Param("periodEnd") LocalDate periodEnd);

    List<HealthReportVO> queryByCondition(HealthReportQueryParam param);

    int deleteById(@Param("id") String id);
}


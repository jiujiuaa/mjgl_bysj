package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.dto.TemperatureLogQueryParam;
import com.zjb.mjgl.pojo.entity.TemperatureLogs;
import com.zjb.mjgl.pojo.vo.TemperatureLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface TemperatureLogMapper {

    int insert(TemperatureLogs entity);

    int update(TemperatureLogs entity);

    int deleteById(@Param("id") String id);

    TemperatureLogs selectById(@Param("id") String id);

    /**
     * 按模具ID查询巡检记录列表
     */
    List<TemperatureLogs> listByMoldId(@Param("moldId") String moldId);

    /**
     * 条件查询（带模具、操作人名称），配合 PageHelper 分页
     */
    List<TemperatureLogVO> queryByCondition(TemperatureLogQueryParam param);

    /**
     * 查询在指定时间之后、温度满足条件的模具ID列表（用于预警规则引擎）。
     * @param since 统计起始时间（operation_time >= since）
     * @param metricType max 或 avg
     * @param compareOp gt, ge, lt, le
     * @param valueThreshold 温度阈值(℃)
     */
    List<String> listMoldIdsWithTemperatureMeetCondition(
            @Param("since") Date since,
            @Param("metricType") String metricType,
            @Param("compareOp") String compareOp,
            @Param("valueThreshold") Double valueThreshold);

    /**
     * 近N天内单条记录温度满足条件的次数≥countThreshold 的模具ID（按次数触发）
     */
    List<String> listMoldIdsWithTemperatureMeetCountGeSince(
            @Param("since") Date since,
            @Param("compareOp") String compareOp,
            @Param("valueThreshold") Double valueThreshold,
            @Param("countThreshold") int countThreshold);
}


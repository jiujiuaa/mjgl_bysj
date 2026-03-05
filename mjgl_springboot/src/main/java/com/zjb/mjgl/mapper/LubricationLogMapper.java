package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.dto.LubricationLogQueryParam;
import com.zjb.mjgl.pojo.entity.LubricationLogs;
import com.zjb.mjgl.pojo.vo.LubricationLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface LubricationLogMapper {

    int insert(LubricationLogs entity);

    int update(LubricationLogs entity);

    int deleteById(@Param("id") String id);

    LubricationLogs selectById(@Param("id") String id);

    /**
     * 按模具ID查询巡检记录列表
     */
    List<LubricationLogs> listByMoldId(@Param("moldId") String moldId);

    /**
     * 条件查询（带模具、操作人名称），配合 PageHelper 分页
     */
    List<LubricationLogVO> queryByCondition(LubricationLogQueryParam param);

    /**
     * 查询在指定时间之后、润滑指标满足条件的模具ID列表（用于预警规则引擎）。
     * @param since 统计起始时间（operation_time >= since）
     * @param metricField 字段名：oil_level_percent 或 pressure_kpa
     * @param metricType min, max 或 avg
     * @param compareOp gt, ge, lt, le
     * @param valueThreshold 数值阈值
     */
    List<String> listMoldIdsWithLubricationMeetCondition(
            @Param("since") Date since,
            @Param("metricField") String metricField,
            @Param("metricType") String metricType,
            @Param("compareOp") String compareOp,
            @Param("valueThreshold") Double valueThreshold);

    /**
     * 近N天内单条记录润滑指标满足条件的次数≥countThreshold 的模具ID（按次数触发）
     */
    List<String> listMoldIdsWithLubricationMeetCountGeSince(
            @Param("since") Date since,
            @Param("metricField") String metricField,
            @Param("compareOp") String compareOp,
            @Param("valueThreshold") Double valueThreshold,
            @Param("countThreshold") int countThreshold);
}


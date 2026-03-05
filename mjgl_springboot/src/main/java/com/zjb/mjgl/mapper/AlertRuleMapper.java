package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.entity.AlertRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlertRuleMapper {

    int insert(AlertRule rule);

    int updateById(AlertRule rule);

    AlertRule selectById(@Param("id") String id);

    AlertRule selectByCode(@Param("code") String code);

    List<AlertRule> selectAll();

    /** 仅查询启用状态、按 sort_order 排序，供规则引擎执行 */
    List<AlertRule> selectEnabledOrderBySortOrder();

    int deleteById(@Param("id") String id);
}

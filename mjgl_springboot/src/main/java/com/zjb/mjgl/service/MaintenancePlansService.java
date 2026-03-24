package com.zjb.mjgl.service;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.MaintenancePlanQueryParam;
import com.zjb.mjgl.pojo.entity.MaintenancePlans;
import com.zjb.mjgl.pojo.vo.MaintenancePlanWithMoldVO;

import java.util.List;

public interface MaintenancePlansService {
    Result<?> insert(MaintenancePlans maintenancePlans);

    Result<?> insertBatch(List<MaintenancePlans> maintenancePlans);

    Result<?> deletePlan(String id);

    Result<?> deletePlansBatch(List<String> ids);

    Result<?> update(MaintenancePlans maintenancePlans);

    /**
     * 分页查询：直接返回带模具信息的 VO
     */
    PageInfo<MaintenancePlanWithMoldVO> query(MaintenancePlanQueryParam maintenancePlanQueryParam,
                                              int pageNum,
                                              int pageSize);

    /**
     * 启用保养计划
     */
    Result<?> enablePlan(String id);

    /**
     * 停用保养计划
     */
    Result<?> disablePlan(String id);
}

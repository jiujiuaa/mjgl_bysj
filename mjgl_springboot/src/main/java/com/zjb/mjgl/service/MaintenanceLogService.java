package com.zjb.mjgl.service;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.MaintenanceLogQueryParam;
import com.zjb.mjgl.pojo.entity.MaintenanceLogs;
import com.zjb.mjgl.pojo.vo.MaintenanceLogVO;

public interface MaintenanceLogService {
    /**
     * 创建保养记录，返回新记录ID
     */
    Result<String> create(MaintenanceLogs maintenanceLogs);

    Result<PageInfo<MaintenanceLogs>> getLogByMoldId(String id, Integer pageNum, Integer pageSize);

    Result<PageInfo<MaintenanceLogs>> getLogByPlanId(String id,Integer pageNum, Integer pageSize);

    /**
     * 根据条件分页查询保养记录（带名称的 VO）
     */
    PageInfo<MaintenanceLogVO> queryByCondition(MaintenanceLogQueryParam param, int pageNum, int pageSize);

    Result<?> delete(String id);

    Result<?> update(MaintenanceLogs maintenanceLogs);

    /**
     * 保养记录合理性审批（仅 ADMIN）：0=未审核, 1=合理, 2=存在问题
     */
    Result<?> approveMaintenanceLog(String id, Integer approvalStatus, String comment);
}

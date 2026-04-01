package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.dto.MaintenanceLogQueryParam;
import com.zjb.mjgl.pojo.entity.MaintenanceLogs;
import com.zjb.mjgl.pojo.vo.MaintenanceLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MaintenanceLogMapper {
    int insert(MaintenanceLogs maintenanceLogs);

    List<MaintenanceLogs> getByMoldId(String id);

    List<MaintenanceLogs> getByPlanId(String id);

    MaintenanceLogs getById(@Param("id") String id);

    void deleteById(String id);

    int update(MaintenanceLogs maintenanceLogs);

    /**
     * 按条件查询保养记录列表（配合 PageHelper 分页）
     */
    List<MaintenanceLogVO> queryByCondition(MaintenanceLogQueryParam param);

    /**
     * 更新保养记录的合理性审批信息
     */
    int updateApproval(@Param("id") String id,
                       @Param("status") Integer status,
                       @Param("comment") String comment,
                       @Param("approverId") String approverId,
                       @Param("approvalTime") java.util.Date approvalTime);
}

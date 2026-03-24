package com.zjb.mjgl.service;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.RepairQueryParam;
import com.zjb.mjgl.pojo.dto.RepairRecordDTO;
import com.zjb.mjgl.pojo.vo.MoldDetailVO;
import com.zjb.mjgl.pojo.vo.RepairRecordVO;

import java.util.List;

public interface RepairService {
    /**
     * 创建维修记录，返回新记录ID（失败返回 null）
     */
    String createRepairRecord(RepairRecordDTO repairRecordDTO);

    Result<?> updeteRepairRecord(RepairRecordDTO repairRecordDTO);

    Result<?> deteleMold(String id);

    Result<?> deleteRecordsBatch(List<String> ids);

    Result<List<RepairRecordVO>> getAllRecord();

    PageInfo<RepairRecordVO> queryByCondition(RepairQueryParam repairQueryParam, int pageNum, int pageSize);

    Result<List<RepairRecordVO>> getByMoldId(String moldid);

    /**
     * 维修记录合理性审批（仅 ADMIN 可操作）：0=未审核, 1=合理, 2=存在问题
     */
    Result<?> approveRepair(String id, Integer approvalStatus, String comment);
}

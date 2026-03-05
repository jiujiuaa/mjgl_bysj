package com.zjb.mjgl.service;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.MoldUsageRecordDTO;
import com.zjb.mjgl.pojo.entity.MoldUsageRecords;
import com.zjb.mjgl.pojo.vo.MoldUsageRecordVO;

import java.util.List;

public interface UseRecordService {

    /**
     * 创建一条模具使用/借出记录
     */
    boolean createRecord(MoldUsageRecordDTO moldUsageRecordDTO);


    Result<String> updateStatus(String id, Integer status);

    /**
     * 根据记录ID查询使用记录
     */
    Result<MoldUsageRecords> getRecordById(String id);

    /**
     * 根据模具ID获取该模具的所有使用记录（按时间排序）
     */
    Result<List<MoldUsageRecordVO>> listByMoldId(String moldId);

    Result<List<MoldUsageRecordVO>> getAllRecord();

    /**
     * 删除一条使用记录，并根据需要同步更新模具状态
     */
    Result<?> deleteRecord(String id);

    Result<?> updateUseRecord(MoldUsageRecordDTO moldUsageRecordDTO);

    /**
     * 合理性审批：1=合理,2=存在问题
     */
    Result<?> approveUsage(String id, Integer approvalStatus, String comment);
}

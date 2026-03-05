package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.dto.RepairQueryParam;
import com.zjb.mjgl.pojo.dto.RepairRecordDTO;
import com.zjb.mjgl.pojo.vo.RepairRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RepairRecordMapper {

    /**
     * 新增一条模具维修记录
     */
    int insert(RepairRecordDTO dto);

    /**
     * 根据ID更新维修记录（只更新非空字段）
     */
    int update(RepairRecordDTO dto);

    /**
     * 根据ID查询单条维修记录
     */
    RepairRecordDTO selectById(String id);

    /**
     * 查询所有维修记录（包含模具与人员信息）
     */
    List<RepairRecordVO> getAll();

    void deleteById(String id);

    List<RepairRecordVO> selectDetailByCondition(RepairQueryParam repairQueryParam);

    List<RepairRecordVO> getrecordByMoldId(@Param("moldId") String moldId);

    /**
     * 更新维修记录的合理性审批信息
     */
    int updateRepairApproval(@Param("id") String id,
                            @Param("status") Integer status,
                            @Param("comment") String comment,
                            @Param("approverId") String approverId,
                            @Param("approveTime") java.time.LocalDateTime approveTime);

    /**
     * 统计某模具在指定时间之后的维修（故障）次数，用于智能预警规则
     */
    int countByMoldIdSince(@Param("moldId") String moldId, @Param("since") java.time.LocalDateTime since);

    /**
     * 查询在指定时间之后维修次数达到阈值的模具ID列表（用于规则引擎批量触发）
     */
    List<String> listMoldIdsWithRepairCountGeSince(@Param("since") java.time.LocalDateTime since, @Param("threshold") int threshold);
}


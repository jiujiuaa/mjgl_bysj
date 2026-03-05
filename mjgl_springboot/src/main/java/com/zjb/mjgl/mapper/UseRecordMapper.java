package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.dto.MoldUsageRecordDTO;
import com.zjb.mjgl.pojo.entity.MoldUsageRecords;
import com.zjb.mjgl.pojo.vo.MoldUsageRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UseRecordMapper {

    /**
     * 新增一条模具使用/借出记录，直接使用 DTO 做参数映射
     */
    int insert(MoldUsageRecordDTO dto);

    /**
     * 根据 id 更新使用记录（实际时间、借用信息、归还信息等）
     */
    int update(MoldUsageRecordDTO dto);

    /**
     * 根据记录ID删除使用记录
     */
    int deleteById(@Param("id") String id);

    /**
     * 统计某个模具下“使用中”状态(2)的使用记录数量
     */
    int countInUseByMoldId(@Param("moldId") String moldId);

    int updateStatus(@Param("id") String id, @Param("status") int status);

    /**
     * 更新使用记录的合理性审批信息
     */
    int updateUsageApproval(@Param("id") String id,
                            @Param("status") Integer status,
                            @Param("comment") String comment,
                            @Param("approverId") String approverId,
                            @Param("approveTime") java.time.LocalDateTime approveTime);

    MoldUsageRecords getRecordById(@Param("id") String id);

    /**
     * 根据模具ID查询该模具的所有使用记录
     */
    List<MoldUsageRecordVO> listByMoldId(@Param("moldId") String moldId);

    /**
     * 查询某个申请人创建的所有使用记录
     */
    List<MoldUsageRecordVO> listByApplicantId(@Param("applicantId") String applicantId);

    /**
     * 查询某个模具在某个申请人名下的所有使用记录
     */
    List<MoldUsageRecordVO> listByMoldIdAndApplicantId(@Param("moldId") String moldId,
                                                       @Param("applicantId") String applicantId);

    List<MoldUsageRecordVO> getAllRecord();
}

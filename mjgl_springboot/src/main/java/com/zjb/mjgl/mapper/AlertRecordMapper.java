package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.dto.AlertRecordQueryParam;
import com.zjb.mjgl.pojo.entity.AlertRecord;
import com.zjb.mjgl.pojo.vo.AlertRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlertRecordMapper {

    int insert(AlertRecord record);

    int update(AlertRecord record);

    AlertRecord selectById(String id);

    AlertRecordVO selectVoById(String id);

    /**
     * 查询同一模具、同一报警类型与触发条件、指定状态的报警（用于去重）
     */
    List<AlertRecord> selectByMoldIdAndAlertTypeAndTriggerConditionAndStatus(
            @Param("moldId") String moldId,
            @Param("alertType") Integer alertType,
            @Param("triggerCondition") String triggerCondition,
            @Param("status") Integer status);

    List<AlertRecordVO> queryByCondition(AlertRecordQueryParam param);
}

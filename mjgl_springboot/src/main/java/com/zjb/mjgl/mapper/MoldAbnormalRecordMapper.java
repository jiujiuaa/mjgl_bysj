package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.dto.MoldAbnormalRecordQueryParam;
import com.zjb.mjgl.pojo.entity.MoldAbnormalRecord;
import com.zjb.mjgl.pojo.vo.MoldAbnormalRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MoldAbnormalRecordMapper {

    int insert(MoldAbnormalRecord record);

    int update(MoldAbnormalRecord record);

    int deleteById(String id);

    MoldAbnormalRecord selectById(String id);

    java.util.List<MoldAbnormalRecord> listByMoldId(String moldId);

    java.util.List<MoldAbnormalRecordVO> queryByCondition(MoldAbnormalRecordQueryParam param);

    /**
     * 统计某模具在指定时间之后的异常记录次数，用于智能预警规则
     */
    int countByMoldIdSince(@Param("moldId") String moldId, @Param("since") java.util.Date since);

    /**
     * 查询在指定时间之后异常次数达到阈值的模具ID列表（用于规则引擎批量触发）
     */
    java.util.List<String> listMoldIdsWithAbnormalCountGeSince(@Param("since") java.util.Date since, @Param("threshold") int threshold);
}


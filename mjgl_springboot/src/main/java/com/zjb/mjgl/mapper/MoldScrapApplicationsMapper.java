package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.entity.MoldScrapApplications;
import com.zjb.mjgl.pojo.vo.MoldScrapApplicationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MoldScrapApplicationsMapper {
    int insert(MoldScrapApplications application);

    MoldScrapApplications selectById(@Param("id") String id);

    /**
     * 查询报废申请列表（按模具/状态可选过滤）
     */
    List<MoldScrapApplicationVO> listByCondition(@Param("moldId") String moldId,
                                                   @Param("status") Integer status);

    int updateApproval(@Param("id") String id,
                        @Param("status") int status,
                        @Param("approvalComment") String approvalComment,
                        @Param("approverId") String approverId,
                        @Param("approverName") String approverName,
                        @Param("approvedAt") java.time.LocalDateTime approvedAt);

    int updateExecute(@Param("id") String id,
                       @Param("status") int status,
                       @Param("handlerId") String handlerId,
                       @Param("handlerName") String handlerName,
                       @Param("handlerComment") String handlerComment,
                       @Param("handledAt") java.time.LocalDateTime handledAt);
}


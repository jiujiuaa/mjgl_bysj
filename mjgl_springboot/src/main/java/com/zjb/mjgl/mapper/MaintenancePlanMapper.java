package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.dto.MaintenancePlanQueryParam;
import com.zjb.mjgl.pojo.entity.MaintenancePlans;
import com.zjb.mjgl.pojo.vo.MaintenancePlanWithMoldVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MaintenancePlanMapper {
    int insert(MaintenancePlans maintenancePlans);

    MaintenancePlans getByMoldId(String moldId);

    int insertBatch(@Param("entities") List<MaintenancePlans> entities);

    int deleteById(String id);

    int update(MaintenancePlans maintenancePlans);

    /**
     * 分页查询：直接连表 molds，返回带模具信息的 VO
     */
    List<MaintenancePlanWithMoldVO> query(MaintenancePlanQueryParam maintenancePlanQueryParam);

    int updateActiveStatus(@Param("id") String id, @Param("isActive") Integer isActive);

    /**
     * 批量取保养计划模板（仅用来健康统计推导 plannedCount）
     */
    List<MaintenancePlans> listByMoldIds(@Param("moldIds") List<String> moldIds);
}

package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.dto.MaintenanceReminderQueryParam;
import com.zjb.mjgl.pojo.entity.MaintenanceReminder;
import com.zjb.mjgl.pojo.vo.MaintenanceReminderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MaintenanceReminderMapper {

    int insert(MaintenanceReminder entity);

    int update(MaintenanceReminder entity);

    MaintenanceReminder selectById(@Param("id") String id);

    int deleteById(@Param("id") String id);

    /**
     * 按条件查询提醒列表（带模具、处理人名称，配合 PageHelper 分页）
     */
    List<MaintenanceReminderVO> queryByCondition(MaintenanceReminderQueryParam param);

    /**
     * 按模具ID查询该模具的提醒列表
     */
    List<MaintenanceReminderVO> listByMoldId(@Param("moldId") String moldId);

    /**
     * 查询全部提醒（用于定时任务）
     */
    List<MaintenanceReminder> listAll();
}

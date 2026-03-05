package com.zjb.mjgl.service;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.MaintenanceReminderQueryParam;
import com.zjb.mjgl.pojo.entity.MaintenanceReminder;
import com.zjb.mjgl.pojo.vo.MaintenanceReminderVO;

import java.util.List;

/**
 * 保养智能提醒服务
 */
public interface MaintenanceReminderService {

    /**
     * 按条件分页查询提醒列表（带模具、处理人名称）
     */
    PageInfo<MaintenanceReminderVO> queryByCondition(MaintenanceReminderQueryParam param, int pageNum, int pageSize);

    /**
     * 按模具ID查询该模具的提醒列表
     */
    Result<List<MaintenanceReminderVO>> listByMoldId(String moldId);

    /**
     * 根据ID查询单条提醒
     */
    Result<MaintenanceReminder> getById(String id);

    /**
     * 主动发送一次提醒消息（不受阈值限制）
     */
    Result<?> sendReminderNow(String id);

    /**
     * 忽略该条提醒（仅对待处理/已发送有效，置为已忽略）
     */
    Result<?> ignoreReminder(String id);
}

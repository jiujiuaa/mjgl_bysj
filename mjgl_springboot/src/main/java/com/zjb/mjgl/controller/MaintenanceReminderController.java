package com.zjb.mjgl.controller;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.MaintenanceReminderQueryParam;
import com.zjb.mjgl.pojo.entity.MaintenanceReminder;
import com.zjb.mjgl.pojo.vo.MaintenanceReminderVO;
import com.zjb.mjgl.service.MaintenanceReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 保养智能提醒接口：加载、查询 maintenance_reminders 表数据
 */
@RestController
@RequestMapping("/api/maintenance-reminder")
@Slf4j
public class MaintenanceReminderController {

    @Resource
    private MaintenanceReminderService maintenanceReminderService;

    /**
     * 按条件分页查询提醒列表（带模具、处理人名称）
     */
    @PostMapping("/query")
    public Result<PageInfo<MaintenanceReminderVO>> query(
            @RequestBody(required = false) MaintenanceReminderQueryParam param,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        MaintenanceReminderQueryParam effective = param != null ? param : new MaintenanceReminderQueryParam();
        log.info("收到保养提醒分页查询请求, pageNum={}, pageSize={}, 条件={}", pageNum, pageSize, effective);
        return Result.success(maintenanceReminderService.queryByCondition(effective, pageNum, pageSize));
    }

    /**
     * 按模具ID查询该模具的提醒列表
     */
    @GetMapping("/list/mold/{moldId}")
    public Result<List<MaintenanceReminderVO>> listByMoldId(@PathVariable String moldId) {
        log.info("收到按模具ID查询保养提醒请求, moldId={}", moldId);
        return maintenanceReminderService.listByMoldId(moldId);
    }

    /**
     * 根据ID查询单条提醒
     */
    @GetMapping("/{id}")
    public Result<MaintenanceReminder> getById(@PathVariable String id) {
        log.info("收到根据ID查询保养提醒请求, id={}", id);
        return maintenanceReminderService.getById(id);
    }

    /**
     * 主动发送一次保养提醒消息
     */
    @PostMapping("/{id}/send")
    public Result<?> sendNow(@PathVariable String id) {
        log.info("收到手动发送保养提醒请求, id={}", id);
        return maintenanceReminderService.sendReminderNow(id);
    }

    /**
     * 忽略该条保养提醒（置为已忽略，定时任务将不再处理）
     */
    @PostMapping("/{id}/ignore")
    public Result<?> ignore(@PathVariable String id) {
        log.info("收到忽略保养提醒请求, id={}", id);
        return maintenanceReminderService.ignoreReminder(id);
    }
}

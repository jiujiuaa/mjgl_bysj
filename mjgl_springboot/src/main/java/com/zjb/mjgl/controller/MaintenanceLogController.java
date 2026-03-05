package com.zjb.mjgl.controller;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.MaintenanceLogQueryParam;
import com.zjb.mjgl.pojo.entity.MaintenanceLogs;
import com.zjb.mjgl.pojo.vo.MaintenanceLogVO;
import com.zjb.mjgl.service.MaintenanceLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/mainlog")
public class MaintenanceLogController {

    @Resource
    private MaintenanceLogService maintenanceLogService;

    @PostMapping("/create")
    public Result<String> create(@RequestBody MaintenanceLogs maintenanceLogs){
        log.info("收到创建保养记录请求, moldId={}, planId={}", maintenanceLogs.getMoldId(), maintenanceLogs.getPlanId());
        return maintenanceLogService.create(maintenanceLogs);
    }

    @GetMapping("/getlog/{id}")
    public Result<PageInfo<MaintenanceLogs>> getRecordById(@PathVariable String id,
                                                           @RequestParam(required = false, defaultValue = "mold") String type,
                                                           @RequestParam(defaultValue = "1") int pageNum,
                                                           @RequestParam(defaultValue = "10") int pageSize) {
        if (id == null || id.trim().isEmpty()) {
            log.warn("根据ID查询保养记录失败, id 为空");
            return Result.fail("id不能为空");
        }
        if("mold".equals(type)){
            return maintenanceLogService.getLogByMoldId(id,pageNum,pageSize);
        }
        else if("plan".equals(type)){
            return maintenanceLogService.getLogByPlanId(id,pageNum,pageSize);
        }
        log.warn("保养记录查询失败, 不支持的类型 type={}", type);
        return Result.fail("类型不符合，请重试");
    }

    /**
     * 根据条件分页查询保养记录
     */
    @PostMapping("/query")
    public Result<PageInfo<MaintenanceLogVO>> query(@RequestBody(required = false) MaintenanceLogQueryParam param,
                                                    @RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "10") int pageSize) {
        MaintenanceLogQueryParam effective = Optional.ofNullable(param)
                .orElseGet(MaintenanceLogQueryParam::new);
        return Result.success(maintenanceLogService.queryByCondition(effective, pageNum, pageSize));
    }

    @DeleteMapping("/deletelog/{id}")
    public Result<?> delete(@PathVariable String id){
        log.info("收到删除保养记录请求, id={}", id);
        return maintenanceLogService.delete(id);
    }

    @PutMapping("/edit")
    public Result<?> update(@RequestBody MaintenanceLogs maintenanceLogs){
        log.info("收到更新保养记录请求, id={}", maintenanceLogs.getId());
        return maintenanceLogService.update(maintenanceLogs);
    }

    /** 保养记录合理性审批（仅 ADMIN） */
    @PostMapping("/log/{id}/approval")
    public Result<?> approval(@PathVariable String id, @RequestBody java.util.Map<String, Object> body) {
        Integer status = null;
        if (body != null && body.get("status") instanceof Number) {
            status = ((Number) body.get("status")).intValue();
        }
        String comment = body != null && body.get("comment") != null ? String.valueOf(body.get("comment")) : null;
        log.info("收到保养记录审批请求, id={}, status={}, comment={}", id, status, comment);
        return maintenanceLogService.approveMaintenanceLog(id, status, comment);
    }
}

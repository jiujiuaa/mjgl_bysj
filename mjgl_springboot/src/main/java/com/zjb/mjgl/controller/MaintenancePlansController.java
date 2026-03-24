package com.zjb.mjgl.controller;


import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.BatchIdsDTO;
import com.zjb.mjgl.pojo.dto.MaintenancePlanQueryParam;
import com.zjb.mjgl.pojo.entity.MaintenancePlans;
import com.zjb.mjgl.pojo.vo.MaintenancePlanWithMoldVO;
import com.zjb.mjgl.service.MaintenancePlansService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/maintenanceplan")
@Slf4j
public class MaintenancePlansController {


    @Resource
    private MaintenancePlansService maintenancePlansService;

    @PostMapping("/add")
    public Result<?> addPlan(@RequestBody MaintenancePlans maintenancePlans) {
        log.info("收到创建保养计划请求, moldId={}, name={}", maintenancePlans.getSpecificMoldId(), maintenancePlans.getName());
        return maintenancePlansService.insert(maintenancePlans);
    }

    @PostMapping("/addbatch")
    public Result<?> addBatchPlans(@RequestBody List<MaintenancePlans> maintenancePlans) {
        log.info("收到批量创建保养计划请求, 数量={}", maintenancePlans != null ? maintenancePlans.size() : 0);
        return maintenancePlansService.insertBatch(maintenancePlans);
    }

    @DeleteMapping("/plan/{id}")
    public Result<?> deleteRecord(@PathVariable String id) {
        log.info("收到删除保养计划请求, id={}", id);
        return maintenancePlansService.deletePlan(id);
    }

    @PostMapping("/plan/batch-delete")
    public Result<?> batchDeletePlans(@RequestBody BatchIdsDTO body) {
        log.info("收到批量删除保养计划请求");
        return maintenancePlansService.deletePlansBatch(body == null ? null : body.getIds());
    }

    @PutMapping("/edit")
    public Result<?> edit(@RequestBody MaintenancePlans maintenancePlans) {
        log.info("收到更新保养计划请求, id={}", maintenancePlans.getId());
        return maintenancePlansService.update(maintenancePlans);
    }

    @PostMapping("/query")
    public Result<PageInfo<MaintenancePlanWithMoldVO>> query(@RequestBody MaintenancePlanQueryParam maintenancePlanQueryParam,
                                                             @RequestParam(defaultValue = "1") int pageNum,
                                                             @RequestParam(defaultValue = "10") int pageSize){
        return Result.success(maintenancePlansService.query(maintenancePlanQueryParam,pageNum,pageSize));
    }

    @PutMapping("/enable/{id}")
    public Result<?> enable(@PathVariable String id) {
        log.info("收到启用保养计划请求, id={}", id);
        return maintenancePlansService.enablePlan(id);
    }

    @PutMapping("/disable/{id}")
    public Result<?> disable(@PathVariable String id) {
        log.info("收到停用保养计划请求, id={}", id);
        return maintenancePlansService.disablePlan(id);
    }

}

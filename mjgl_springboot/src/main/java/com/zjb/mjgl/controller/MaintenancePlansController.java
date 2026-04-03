package com.zjb.mjgl.controller;


import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.BatchIdsDTO;
import com.zjb.mjgl.pojo.dto.MaintenancePlanQueryParam;
import com.zjb.mjgl.pojo.entity.MaintenancePlans;
import com.zjb.mjgl.pojo.vo.MaintenancePlanWithMoldVO;
import com.zjb.mjgl.service.MaintenancePlansService;
import com.zjb.mjgl.web.DynamicPageSize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/maintenanceplan")
@Slf4j
@PreAuthorize("isAuthenticated()")
public class MaintenancePlansController {


    @Resource
    private MaintenancePlansService maintenancePlansService;

    @PostMapping("/add")
    public Result<?> addPlan(@RequestBody MaintenancePlans maintenancePlans) {
        try {
            log.info("收到创建保养计划请求, moldId={}, name={}", maintenancePlans.getSpecificMoldId(), maintenancePlans.getName());
            return maintenancePlansService.insert(maintenancePlans);
        } catch (Exception e) {
            log.error("创建保养计划异常", e);
            return Result.fail("创建保养计划失败: " + e.getMessage());
        }
    }

    @PostMapping("/addbatch")
    public Result<?> addBatchPlans(@RequestBody List<MaintenancePlans> maintenancePlans) {
        try {
            log.info("收到批量创建保养计划请求, 数量={}", maintenancePlans != null ? maintenancePlans.size() : 0);
            return maintenancePlansService.insertBatch(maintenancePlans);
        } catch (Exception e) {
            log.error("批量创建保养计划异常", e);
            return Result.fail("批量创建保养计划失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/plan/{id}")
    public Result<?> deleteRecord(@PathVariable String id) {
        try {
            log.info("收到删除保养计划请求, id={}", id);
            return maintenancePlansService.deletePlan(id);
        } catch (Exception e) {
            log.error("删除保养计划异常, id={}", id, e);
            return Result.fail("删除保养计划失败: " + e.getMessage());
        }
    }

    @PostMapping("/plan/batch-delete")
    public Result<?> batchDeletePlans(@RequestBody BatchIdsDTO body) {
        try {
            log.info("收到批量删除保养计划请求");
            return maintenancePlansService.deletePlansBatch(body == null ? null : body.getIds());
        } catch (Exception e) {
            log.error("批量删除保养计划异常", e);
            return Result.fail("批量删除保养计划失败: " + e.getMessage());
        }
    }

    @PutMapping("/edit")
    public Result<?> edit(@RequestBody MaintenancePlans maintenancePlans) {
        try {
            log.info("收到更新保养计划请求, id={}", maintenancePlans.getId());
            return maintenancePlansService.update(maintenancePlans);
        } catch (Exception e) {
            log.error("更新保养计划异常, id={}", maintenancePlans == null ? null : maintenancePlans.getId(), e);
            return Result.fail("更新保养计划失败: " + e.getMessage());
        }
    }

    @PostMapping("/query")
    public Result<PageInfo<MaintenancePlanWithMoldVO>> query(@RequestBody MaintenancePlanQueryParam maintenancePlanQueryParam,
                                                             @RequestParam(defaultValue = "1") int pageNum,
                                                             @DynamicPageSize int pageSize){
        try {
            return Result.success(maintenancePlansService.query(maintenancePlanQueryParam,pageNum,pageSize));
        } catch (Exception e) {
            log.error("查询保养计划异常, pageNum={}, pageSize={}", pageNum, pageSize, e);
            return Result.fail("查询保养计划失败: " + e.getMessage());
        }
    }

    @PutMapping("/enable/{id}")
    public Result<?> enable(@PathVariable String id) {
        try {
            log.info("收到启用保养计划请求, id={}", id);
            return maintenancePlansService.enablePlan(id);
        } catch (Exception e) {
            log.error("启用保养计划异常, id={}", id, e);
            return Result.fail("启用保养计划失败: " + e.getMessage());
        }
    }

    @PutMapping("/disable/{id}")
    public Result<?> disable(@PathVariable String id) {
        try {
            log.info("收到停用保养计划请求, id={}", id);
            return maintenancePlansService.disablePlan(id);
        } catch (Exception e) {
            log.error("停用保养计划异常, id={}", id, e);
            return Result.fail("停用保养计划失败: " + e.getMessage());
        }
    }

}

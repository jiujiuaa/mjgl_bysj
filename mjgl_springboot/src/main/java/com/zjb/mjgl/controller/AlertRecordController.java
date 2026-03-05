package com.zjb.mjgl.controller;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.AlertRecordQueryParam;
import com.zjb.mjgl.pojo.vo.AlertRecordVO;
import com.zjb.mjgl.service.AlertRecordService;
import com.zjb.mjgl.service.AlertRuleEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 报警记录：智能预警结果列表与状态闭环（已解决/已忽略）
 */
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertRecordController {

    private final AlertRecordService alertRecordService;
    private final AlertRuleEngineService alertRuleEngineService;

    /**
     * 分页条件查询报警记录（可按状态、模具、规则筛选）
     */
    @PostMapping("/query")
    public Result<PageInfo<AlertRecordVO>> query(
            @RequestBody(required = false) AlertRecordQueryParam param,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        AlertRecordQueryParam effective = Optional.ofNullable(param).orElseGet(AlertRecordQueryParam::new);
        return Result.success(alertRecordService.queryByCondition(effective, pageNum, pageSize));
    }

    /**
     * 根据ID查询单条报警
     */
    @GetMapping("/{id}")
    public Result<AlertRecordVO> getById(@PathVariable String id) {
        return alertRecordService.getById(id);
    }

    /**
     * 将报警标记为「已解决」
     */
    @PutMapping("/{id}/resolve")
    public Result<Void> resolve(@PathVariable String id, @RequestParam(required = false) String remark) {
        return alertRecordService.resolve(id, remark);
    }

    /**
     * 将报警标记为「已忽略」
     */
    @PutMapping("/{id}/ignore")
    public Result<Void> ignore(@PathVariable String id, @RequestParam(required = false) String remark) {
        return alertRecordService.ignore(id, remark);
    }

    /**
     * 手动触发一次智能预警规则引擎（供管理员或定时任务调用）
     */
    @PostMapping("/run-rules")
    public Result<Void> runRules() {
        alertRuleEngineService.runAllRules();
        return Result.success();
    }
}

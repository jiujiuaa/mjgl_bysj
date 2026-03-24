package com.zjb.mjgl.controller;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.AlertRuleSaveParam;
import com.zjb.mjgl.pojo.dto.BatchIdsDTO;
import com.zjb.mjgl.pojo.vo.AlertRuleVO;
import com.zjb.mjgl.service.AlertRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能预警规则：手动定制规则（增删改查、启用/禁用、初始化默认）
 */
@RestController
@RequestMapping("/api/alert-rules")
@RequiredArgsConstructor
public class AlertRuleController {

    private final AlertRuleService alertRuleService;

    @GetMapping("/list")
    public Result<List<AlertRuleVO>> list() {
        return Result.success(alertRuleService.listAll());
    }

    @GetMapping("/{id}")
    public Result<AlertRuleVO> getById(@PathVariable String id) {
        return Result.success(alertRuleService.getById(id));
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody @Validated AlertRuleSaveParam param) {
        return Result.success(alertRuleService.save(param));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        alertRuleService.deleteById(id);
        return Result.success();
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody BatchIdsDTO body) {
        try {
            alertRuleService.deleteByIds(body == null ? null : body.getIds());
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    @PutMapping("/{id}/enabled")
    public Result<Void> setEnabled(@PathVariable String id, @RequestParam Integer enabled) {
        alertRuleService.setEnabled(id, enabled);
        return Result.success();
    }

    /** 初始化默认规则（若不存在则插入两条：近30天故障≥3次、近30天异常≥3次） */
    @PostMapping("/init-defaults")
    public Result<Integer> initDefaults() {
        int added = alertRuleService.initDefaults();
        return Result.success(added);
    }
}

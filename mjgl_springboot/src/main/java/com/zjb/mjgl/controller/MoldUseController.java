package com.zjb.mjgl.controller;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.MoldParam;
import com.zjb.mjgl.pojo.dto.MoldUsageRecordDTO;
import com.zjb.mjgl.service.UseRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/molduse")
public class MoldUseController {

    @Autowired
    private UseRecordService useRecordService;

    /**
     * 创建模具使用/借出记录
     */
    @PostMapping("/create")
    public Result<Void> createUseRecord(@RequestBody MoldUsageRecordDTO moldUsageRecordDTO) {
        if (moldUsageRecordDTO == null || moldUsageRecordDTO.getMoldId() == null) {
            return Result.fail("模具ID不能为空");
        }
        boolean success = useRecordService.createRecord(moldUsageRecordDTO);
        if (success) {
            return Result.success();
        }
        return Result.fail("创建失败,请检查模具状态");
    }
    @PutMapping("/record/{id}/status")
    public Result<String> updateUserStatus(@PathVariable String id, @RequestBody Map<String, Integer> requestBody) {
        try {
            Integer status = requestBody.get("status");
            if (status == null ) {
                return Result.fail("状态不能为空");
            }
            return useRecordService.updateStatus(id, status);
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            return Result.fail("更新用户状态失败: " + e.getMessage());
        }
    }

    /**
     * 根据记录ID查询模具使用/借出记录
     */
    @GetMapping("/record/{id}")
    public Result<?> getRecordById(@PathVariable String id) {
        if (id == null || id.trim().isEmpty()) {
            return Result.fail("记录ID不能为空");
        }
        return useRecordService.getRecordById(id);
    }

    /**
     * 根据模具ID获取该模具的所有使用记录（按时间倒序）
     */
    @GetMapping("/record/mold/{moldId}")
    public Result<?> listByMoldId(@PathVariable String moldId) {
        if (moldId == null || moldId.trim().isEmpty()) {
            return Result.fail("模具ID不能为空");
        }
        return useRecordService.listByMoldId(moldId);
    }
    @GetMapping("/getall")
    public Result<?> getAllRecord()
    {
        return useRecordService.getAllRecord();
    }

    /**
     * 删除使用记录，并同步更新模具状态
     */
    @DeleteMapping("/record/{id}")
    public Result<?> deleteRecord(@PathVariable String id) {
        return useRecordService.deleteRecord(id);
    }

    @PutMapping("/updateRecord")
    public Result<?> updateMold(@RequestBody MoldUsageRecordDTO moldUsageRecordDTO) {
        if (moldUsageRecordDTO.getMoldId() == null || moldUsageRecordDTO.getMoldId().trim().isEmpty()) {
            return Result.fail("模具ID不能为空");
        }
        if(moldUsageRecordDTO.getId()==null||moldUsageRecordDTO.getId().trim().isEmpty()){
            return Result.fail("记录ID不能为空");
        }
        return useRecordService.updateUseRecord(moldUsageRecordDTO);

    }
}

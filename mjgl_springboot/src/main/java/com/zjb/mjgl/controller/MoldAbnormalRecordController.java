package com.zjb.mjgl.controller;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.MoldAbnormalRecordQueryParam;
import com.zjb.mjgl.pojo.entity.MoldAbnormalRecord;
import com.zjb.mjgl.pojo.vo.MoldAbnormalRecordVO;
import com.zjb.mjgl.service.MoldAbnormalRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/mold-abnormal")
@RequiredArgsConstructor
public class MoldAbnormalRecordController {

    private final MoldAbnormalRecordService moldAbnormalRecordService;

    /**
     * 人工录入异常上报
     */
    @PostMapping("/manual-create")
    public Result<String> manualCreate(@RequestBody MoldAbnormalRecord record) {
        if (record == null || record.getMoldId() == null) {
            log.warn("manualCreate: 请求体为空或模具ID缺失");
            return Result.fail("模具ID不能为空");
        }
        log.info("manualCreate: 人工异常上报, moldId={}", record.getMoldId());
        return moldAbnormalRecordService.createManual(record);
    }

    /**
     * 条件分页查询异常记录（VO）
     */
    @PostMapping("/query")
    public Result<PageInfo<MoldAbnormalRecordVO>> query(
            @RequestBody(required = false) MoldAbnormalRecordQueryParam param,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        MoldAbnormalRecordQueryParam effective = Optional.ofNullable(param)
                .orElseGet(MoldAbnormalRecordQueryParam::new);
        return Result.success(moldAbnormalRecordService.queryByCondition(effective, pageNum, pageSize));
    }

    /**
     * 按模具ID分页查询异常记录（实体）
     */
    @GetMapping("/mold/{moldId}")
    public Result<PageInfo<MoldAbnormalRecord>> listByMold(
            @PathVariable String moldId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return moldAbnormalRecordService.getByMoldId(moldId, pageNum, pageSize);
    }

    /**
     * 更新异常记录
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody MoldAbnormalRecord record) {
        return moldAbnormalRecordService.update(record);
    }

    /**
     * 删除异常记录
     */
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable String id) {
        return moldAbnormalRecordService.delete(id);
    }
}



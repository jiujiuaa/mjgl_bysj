package com.zjb.mjgl.controller;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.LubricationLogQueryParam;
import com.zjb.mjgl.pojo.entity.LubricationLogs;
import com.zjb.mjgl.pojo.vo.LubricationLogVO;
import com.zjb.mjgl.service.LubricationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/lubrication-log")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','INSPECTOR')")
public class LubricationLogController {

    private final LubricationLogService lubricationLogService;

    @PostMapping("/create")
    public Result<String> create(@RequestBody LubricationLogs record) {
        if (record == null || record.getMoldId() == null) {
            log.warn("创建润滑巡检记录失败, 请求体为空或模具ID缺失");
            return Result.fail("模具ID不能为空");
        }
        log.info("收到创建润滑巡检记录请求, moldId={}", record.getMoldId());
        return lubricationLogService.create(record);
    }

    @PostMapping("/query")
    public Result<PageInfo<LubricationLogVO>> query(@RequestBody(required = false) LubricationLogQueryParam param,
                                                    @RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "10") int pageSize) {
        LubricationLogQueryParam effective = Optional.ofNullable(param)
                .orElseGet(LubricationLogQueryParam::new);
        return Result.success(lubricationLogService.queryByCondition(effective, pageNum, pageSize));
    }

    @GetMapping("/mold/{moldId}")
    public Result<PageInfo<LubricationLogs>> listByMold(@PathVariable String moldId,
                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                        @RequestParam(defaultValue = "10") int pageSize) {
        return lubricationLogService.getByMoldId(moldId, pageNum, pageSize);
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody LubricationLogs log) {
        return lubricationLogService.update(log);
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable String id) {
        return lubricationLogService.delete(id);
    }
}


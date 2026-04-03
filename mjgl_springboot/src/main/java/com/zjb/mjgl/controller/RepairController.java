package com.zjb.mjgl.controller;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.BatchIdsDTO;
import com.zjb.mjgl.pojo.dto.RepairQueryParam;
import com.zjb.mjgl.pojo.dto.RepairRecordDTO;
import com.zjb.mjgl.pojo.vo.RepairRecordVO;
import com.zjb.mjgl.service.RepairService;
import com.zjb.mjgl.web.DynamicPageSize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/repair")
@PreAuthorize("isAuthenticated()")
public class RepairController {

    @Autowired
    private RepairService repairService;

    @PostMapping("/create")
    public Result<String> create(@RequestBody RepairRecordDTO repairRecordDTO) {
        if (repairRecordDTO == null || repairRecordDTO.getMoldId() == null) {
            log.warn("创建维修记录失败, 请求体为空或模具ID缺失");
            return Result.fail("模具ID不能为空");
        }
        log.info("收到创建维修记录请求, moldId={}", repairRecordDTO.getMoldId());
        String id = repairService.createRepairRecord(repairRecordDTO);
        if (id != null) {
            log.info("创建维修记录成功, id={}", id);
            return Result.success(id);
        }
        log.warn("创建维修记录失败, moldId={}", repairRecordDTO.getMoldId());
        return Result.fail("创建失败,请检查模具状态");
    }

    @PutMapping("/update")
    public Result<?> update(@RequestBody RepairRecordDTO repairRecordDTO) {
        String id = Optional.ofNullable(repairRecordDTO)
                .map(RepairRecordDTO::getId)
                .orElse(null);
        log.info("收到更新维修记录请求, id={}", id);
        return repairService.updeteRepairRecord(repairRecordDTO);
    }

    @DeleteMapping("/record/{id}")
    public Result<?> delete(@PathVariable String id) {
        log.info("收到删除维修记录请求, id={}", id);
        return repairService.deteleMold(id);
    }

    @PostMapping("/record/batch-delete")
    public Result<?> batchDeleteRecords(@RequestBody BatchIdsDTO body) {
        log.info("收到批量删除维修记录请求");
        return repairService.deleteRecordsBatch(body == null ? null : body.getIds());
    }

    @GetMapping("/getAll")
    public Result<List<RepairRecordVO>> getAll() {
        log.info("收到查询全部维修记录请求");
        return repairService.getAllRecord();
    }

    /** 条件分页查询：使用 POST 以便通过 body 传复杂条件 */
    @PostMapping("/query")
    public Result<PageInfo<RepairRecordVO>> query(@RequestBody(required = false) RepairQueryParam repairQueryParam,
                                                  @RequestParam(defaultValue = "1") int pageNum,
                                                  @DynamicPageSize int pageSize) {
        RepairQueryParam effective = Optional.ofNullable(repairQueryParam)
                .orElseGet(RepairQueryParam::new);
        return Result.success(repairService.queryByCondition(effective, pageNum, pageSize));
    }

    /** 根据模具ID查询该模具的维修记录列表 */
    @GetMapping("/record/mold/{moldId}")
    public Result<?> getMoldRecord(@PathVariable String moldId) {
        if (moldId == null || moldId.trim().isEmpty()) {
            log.warn("按模具ID查询维修记录失败, moldId 为空");
            return Result.fail("模具ID不能为空");
        }
        String trimmedId = moldId.trim();
        log.info("收到按模具ID查询维修记录请求, moldId={}", trimmedId);
        return repairService.getByMoldId(trimmedId);
    }

    /** 维修记录合理性审批（仅 ADMIN） */
    @PostMapping("/record/{id}/approval")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> approval(@PathVariable String id, @RequestBody java.util.Map<String, Object> body) {
        Integer status = null;
        if (body != null && body.get("status") instanceof Number) {
            status = ((Number) body.get("status")).intValue();
        }
        String comment = body != null && body.get("comment") != null ? String.valueOf(body.get("comment")) : null;
        log.info("收到维修记录审批请求, id={}, status={}, comment={}", id, status, comment);
        return repairService.approveRepair(id, status, comment);
    }
}

package com.zjb.mjgl.controller;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.MoldScrapApplicationDTO;
import com.zjb.mjgl.pojo.vo.MoldScrapApplicationDetailVO;
import com.zjb.mjgl.pojo.vo.MoldScrapApplicationVO;
import com.zjb.mjgl.service.MoldScrapApplicationsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/moldscrap")
@PreAuthorize("isAuthenticated()")
public class MoldScrapApplicationsController {

    private final MoldScrapApplicationsService moldScrapApplicationsService;

    public MoldScrapApplicationsController(MoldScrapApplicationsService moldScrapApplicationsService) {
        this.moldScrapApplicationsService = moldScrapApplicationsService;
    }

    @PostMapping("/create")
    public Result<String> create(@RequestBody MoldScrapApplicationDTO dto) {
        log.info("收到创建报废申请请求, moldId={}", dto == null ? null : dto.getMoldId());
        return moldScrapApplicationsService.create(dto);
    }

    /**
     * 报废申请审批（仅 ADMIN）
     * body：
     * - status：2=已批准, 3=已拒绝
     * - comment：审批意见（可选）
     */
    @PutMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> approve(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Integer status = null;
        if (body != null && body.get("status") instanceof Number) {
            status = ((Number) body.get("status")).intValue();
        }
        String comment = body == null ? null : Optional.ofNullable(body.get("comment")).map(String::valueOf).orElse(null);
        return moldScrapApplicationsService.approve(id, status, comment);
    }

    /**
     * 报废执行
     * body：
     * - comment：执行备注/处理意见（可选）
     */
    @PutMapping("/execute/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> execute(@PathVariable String id, @RequestBody(required = false) Map<String, Object> body) {
        String comment = body == null ? null : Optional.ofNullable(body.get("comment")).map(String::valueOf).orElse(null);
        return moldScrapApplicationsService.execute(id, comment);
    }

    @GetMapping("/detail/{id}")
    public Result<MoldScrapApplicationDetailVO> detail(@PathVariable String id) {
        return moldScrapApplicationsService.getDetail(id);
    }

    @GetMapping("/list")
    public Result<List<MoldScrapApplicationVO>> list(
            @RequestParam(required = false) String moldId,
            @RequestParam(required = false) Integer status) {
        return moldScrapApplicationsService.listByCondition(moldId, status);
    }
}


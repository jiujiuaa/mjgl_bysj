package com.zjb.mjgl.controller;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.BatchIdsDTO;
import com.zjb.mjgl.pojo.dto.MoldParam;
import com.zjb.mjgl.pojo.dto.MoldQueryParam;
import com.zjb.mjgl.pojo.vo.MoldDetailVO;
import com.zjb.mjgl.service.MoldService;
import com.zjb.mjgl.web.DynamicPageSize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/molds")
@PreAuthorize("isAuthenticated()")
public class MoldController {

    @Autowired
    private MoldService moldService;

    /**
     * 创建模具（主表 + 技术参数 + 二维码），返回完整详情 VO
     */
    @PostMapping("/create")
    public Result<MoldDetailVO> create(@RequestBody MoldParam param) {
        try {
            MoldDetailVO vo = moldService.createMold(param);
            return Result.success(vo);
        } catch (Exception e) {
            log.error("创建模具异常", e);
            return Result.fail("创建模具失败: " + e.getMessage());
        }
    }

    /**
     * 更新模具基础信息 + 技术参数 + 二维码
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody MoldParam param) {
        try {
            moldService.updateMold(param);
            return Result.success();
        } catch (Exception e) {
            log.error("更新模具异常, id={}", param == null ? null : param.getId(), e);
            return Result.fail("更新模具失败: " + e.getMessage());
        }
    }
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        try {
            moldService.deteleMold(id);
            return Result.success(id);
        } catch (Exception e) {
            log.error("删除模具异常, id={}", id, e);
            return Result.fail("删除模具失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody BatchIdsDTO body) {
        try {
            List<String> ids = BatchIdsDTO.normalizeList(body == null ? null : body.getIds());
            if (ids.isEmpty()) {
                return Result.fail("请选择要删除的模具");
            }
            moldService.deleteMoldsBatch(ids);
            return Result.success();
        } catch (Exception e) {
            log.error("批量删除模具异常", e);
            return Result.fail("批量删除模具失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询所有模具（主表 + specs + 一个 qrcode + files）
     * 参数：pageNum 页码（从 1 开始），pageSize 每页条数
     */
    @GetMapping("/allmolds")
    public Result<PageInfo<MoldDetailVO>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @DynamicPageSize int pageSize) {
        try {
            return Result.success(moldService.listAllAsDetail(pageNum, pageSize));
        } catch (Exception e) {
            log.error("分页查询模具异常, pageNum={}, pageSize={}", pageNum, pageSize, e);
            return Result.fail("查询模具列表失败: " + e.getMessage());
        }
    }

    /**
     * 按条件分页查询模具
     */
    @PostMapping("/query")
    public Result<PageInfo<MoldDetailVO>> query(@RequestBody MoldQueryParam param,
                                               @RequestParam(defaultValue = "1") int pageNum,
                                               @DynamicPageSize int pageSize) {
        try {
            return Result.success(moldService.queryByCondition(param, pageNum, pageSize));
        } catch (Exception e) {
            log.error("条件查询模具异常, pageNum={}, pageSize={}", pageNum, pageSize, e);
            return Result.fail("条件查询模具失败: " + e.getMessage());
        }
    }
}

package com.zjb.mjgl.controller;

import com.github.pagehelper.PageInfo;
import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.BatchIdsDTO;
import com.zjb.mjgl.pojo.dto.MoldParam;
import com.zjb.mjgl.pojo.dto.MoldQueryParam;
import com.zjb.mjgl.pojo.vo.MoldDetailVO;
import com.zjb.mjgl.service.MoldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/molds")
public class MoldController {

    @Autowired
    private MoldService moldService;

    /**
     * 创建模具（主表 + 技术参数 + 二维码），返回完整详情 VO
     */
    @PostMapping("/create")
    public Result<MoldDetailVO> create(@RequestBody MoldParam param) {
        MoldDetailVO vo = moldService.createMold(param);
        return Result.success(vo);
    }

    /**
     * 更新模具基础信息 + 技术参数 + 二维码
     */
    @PutMapping("/update")
    public Result<Void> update(@RequestBody MoldParam param) {
        moldService.updateMold(param);
        return Result.success();
    }
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
            moldService.deteleMold(id);
        return Result.success(id);
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody BatchIdsDTO body) {
        List<String> ids = BatchIdsDTO.normalizeList(body == null ? null : body.getIds());
        if (ids.isEmpty()) {
            return Result.fail("请选择要删除的模具");
        }
        moldService.deleteMoldsBatch(ids);
        return Result.success();
    }

    /**
     * 分页查询所有模具（主表 + specs + 一个 qrcode + files）
     * 参数：pageNum 页码（从 1 开始），pageSize 每页条数
     */
    @GetMapping("/allmolds")
    public Result<PageInfo<MoldDetailVO>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(moldService.listAllAsDetail(pageNum, pageSize));
    }

    /**
     * 按条件分页查询模具
     */
    @PostMapping("/query")
    public Result<PageInfo<MoldDetailVO>> query(@RequestBody MoldQueryParam param,
                                               @RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(moldService.queryByCondition(param, pageNum, pageSize));
    }
}

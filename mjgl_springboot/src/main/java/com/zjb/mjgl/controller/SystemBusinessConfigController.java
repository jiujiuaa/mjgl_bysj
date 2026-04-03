package com.zjb.mjgl.controller;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.BusinessConfigBatchUpdateDTO;
import com.zjb.mjgl.pojo.vo.BusinessConfigItemVO;
import com.zjb.mjgl.service.SystemBusinessConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/business-config")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class SystemBusinessConfigController {

    private final SystemBusinessConfigService systemBusinessConfigService;

    @GetMapping
    public Result<List<BusinessConfigItemVO>> list() {
        return Result.success(systemBusinessConfigService.listAllForAdmin());
    }

    @PutMapping
    public Result<Void> update(@RequestBody BusinessConfigBatchUpdateDTO body) {
        try {
            systemBusinessConfigService.updateBatch(body);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }
}

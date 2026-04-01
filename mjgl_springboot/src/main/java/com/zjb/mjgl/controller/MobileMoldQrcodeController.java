package com.zjb.mjgl.controller;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.MoldQrcodeMobileResolveRequest;
import com.zjb.mjgl.service.MoldQrcodeMobileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 移动端：扫码后统一入口（先扫码后选动作）
 */
@RestController
@RequestMapping("/api/mobile/mold-qrcodes")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class MobileMoldQrcodeController {

    private final MoldQrcodeMobileService moldQrcodeMobileService;

    @PostMapping("/resolve")
    public Result<?> resolve(@RequestBody(required = false) MoldQrcodeMobileResolveRequest request) {
        try {
            log.info("mobile resolve request: codeId={}, scanType={}",
                    request == null ? null : request.getCodeId(),
                    request == null ? null : request.getScanType());
            return moldQrcodeMobileService.resolve(request);
        } catch (Exception e) {
            log.error("移动端二维码解析异常, codeId={}", request == null ? null : request.getCodeId(), e);
            return Result.fail("二维码解析失败: " + e.getMessage());
        }
    }
}


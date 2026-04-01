package com.zjb.mjgl.controller;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.SendAlertToUserDTO;
import com.zjb.mjgl.pojo.entity.MoldAlertMessage;
import com.zjb.mjgl.service.AlertMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * WebSocket 测试/发送接口
 * 提供 REST 接口触发“发送一条 WebSocket 消息”，便于调试和学习：前端订阅 /topic/alerts 后，调用本接口即可收到推送。
 */
@RestController
@RequestMapping("/api/ws")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class WebSocketController {

    private final AlertMessageService alertMessageService;

    /**
     * 发送一条模具告警消息到 WebSocket 主题 /topic/alerts
     * 前端需先建立连接并订阅 /topic/alerts，即可收到推送。
     */
    @PostMapping("/send")
    public Result<Void> sendAlert(@RequestBody MoldAlertMessage message) {
        try {
            alertMessageService.broadcastAlert(message);
            return Result.success();
        } catch (Exception e) {
            log.error("发送广播告警异常", e);
            return Result.fail("发送广播告警失败: " + e.getMessage());
        }
    }

    /**
     * 按用户ID单播一条告警消息（路径传参）：
     * 前端需订阅 /topic/alerts.user.{userId} 才能收到。
     */
    @PostMapping("/sendToUser/{userId}")
    public Result<Void> sendAlertToUserPath(@PathVariable String userId, @RequestBody MoldAlertMessage message) {
        try {
            alertMessageService.sendAlertToUser(userId, message);
            return Result.success();
        } catch (Exception e) {
            log.error("单播告警异常, userId={}", userId, e);
            return Result.fail("单播告警失败: " + e.getMessage());
        }
    }

    /**
     * 按用户ID单播一条告警消息（请求体传 userId，避免路径被截断导致 404）：
     * POST /api/ws/sendToUser ，Body 里带 userId、title、content、type、biz_type 等。
     */
    @PostMapping("/sendToUser")
    public Result<Void> sendAlertToUserBody(@RequestBody SendAlertToUserDTO dto) {
        try {
            if (dto.getUserId() == null || dto.getUserId().trim().isEmpty()) {
                return Result.fail("userId 不能为空");
            }
            alertMessageService.sendAlertByDto(dto);
            return Result.success();
        } catch (Exception e) {
            log.error("按请求体发送告警异常, userId={}", dto == null ? null : dto.getUserId(), e);
            return Result.fail("按请求体发送告警失败: " + e.getMessage());
        }
    }
}

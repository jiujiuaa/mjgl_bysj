package com.zjb.mjgl.controller;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.entity.UserNotification;
import com.zjb.mjgl.service.NotificationService;
import com.zjb.mjgl.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 查询当前登录用户的未读通知列表
     */
    @GetMapping("/unread")
    public Result<List<UserNotification>> listUnread() {
        String userId = UserUtils.getCurrentUserId();
        if (userId == null) {
            log.warn("查询未读通知失败, 用户未登录");
            return Result.fail(401, "未登录");
        }
        List<UserNotification> list = notificationService.listUnread(userId);
        return Result.success(list);
    }

    /**
     * 查询当前登录用户的全部通知列表（包含已读与未读）
     */
    @GetMapping("/all")
    public Result<List<UserNotification>> listAll() {
        String userId = UserUtils.getCurrentUserId();
        if (userId == null) {
            log.warn("查询全部通知失败, 用户未登录");
            return Result.fail(401, "未登录");
        }
        List<UserNotification> list = notificationService.listAll(userId);
        return Result.success(list);
    }

    /**
     * 将某条通知标记为已读
     */
    @PostMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable String id) {
        log.info("标记通知为已读, id={}", id);
        notificationService.markRead(id);
        return Result.success();
    }
}


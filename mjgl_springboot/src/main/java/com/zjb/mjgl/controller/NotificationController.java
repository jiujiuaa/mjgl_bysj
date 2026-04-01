package com.zjb.mjgl.controller;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.entity.UserNotification;
import com.zjb.mjgl.service.NotificationService;
import com.zjb.mjgl.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 查询当前登录用户的未读通知列表
     */
    @GetMapping("/unread")
    public Result<List<UserNotification>> listUnread() {
        try {
            String userId = UserUtils.getCurrentUserId();
            if (userId == null) {
                log.warn("查询未读通知失败, 用户未登录");
                return Result.fail(401, "未登录");
            }
            List<UserNotification> list = notificationService.listUnread(userId);
            return Result.success(list);
        } catch (Exception e) {
            log.error("查询未读通知异常", e);
            return Result.fail("查询未读通知失败: " + e.getMessage());
        }
    }

    /**
     * 查询当前登录用户的全部通知列表（包含已读与未读）
     */
    @GetMapping("/all")
    public Result<List<UserNotification>> listAll() {
        try {
            String userId = UserUtils.getCurrentUserId();
            if (userId == null) {
                log.warn("查询全部通知失败, 用户未登录");
                return Result.fail(401, "未登录");
            }
            List<UserNotification> list = notificationService.listAll(userId);
            return Result.success(list);
        } catch (Exception e) {
            log.error("查询全部通知异常", e);
            return Result.fail("查询全部通知失败: " + e.getMessage());
        }
    }

    /**
     * 将某条通知标记为已读
     */
    @PostMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable String id) {
        try {
            log.info("标记通知为已读, id={}", id);
            notificationService.markRead(id);
            return Result.success();
        } catch (Exception e) {
            log.error("标记通知已读异常, id={}", id, e);
            return Result.fail("标记已读失败: " + e.getMessage());
        }
    }

    /**
     * 将当前用户全部未读通知标记为已读
     */
    @PostMapping("/read-all")
    public Result<Void> markAllRead() {
        try {
            String userId = UserUtils.getCurrentUserId();
            if (userId == null) {
                log.warn("全部已读失败, 用户未登录");
                return Result.fail(401, "未登录");
            }
            notificationService.markAllRead(userId);
            return Result.success();
        } catch (Exception e) {
            log.error("全部通知标记已读异常", e);
            return Result.fail("全部已读失败: " + e.getMessage());
        }
    }
}


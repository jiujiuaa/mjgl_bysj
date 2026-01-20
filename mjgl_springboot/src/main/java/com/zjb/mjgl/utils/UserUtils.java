package com.zjb.mjgl.utils;

import com.zjb.mjgl.common.UserDetailsImpl;
import com.zjb.mjgl.pojo.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 获取当前登录用户的工具类（纯静态，无需注入，无需 @Component）
 */
public final class UserUtils { // 建议加 final 防止继承

    private UserUtils() {} // 私有构造，防止实例化

    /**
     * 获取当前登录的 UserDetailsImpl（如果未登录则返回 null）
     */
    public static UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return (UserDetailsImpl) principal;
        }
        return null;
    }

    /**
     * 获取当前用户的 User 对象（业务实体）
     */
    public static User getCurrentUserDetails() {
        UserDetailsImpl userDetails = getCurrentUser();
        return userDetails != null ? userDetails.getUser() : null;
    }

    /**
     * 获取当前用户的 ID（常用！）
     */
    public static String getCurrentUserId() {
        User user = getCurrentUserDetails();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前用户名（username）
     */
    public static String getCurrentUsername() {
        UserDetailsImpl userDetails = getCurrentUser();
        return userDetails != null ? userDetails.getUsername() : null;
    }
}
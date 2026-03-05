package com.zjb.mjgl.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 VO
 */
@Data
@NoArgsConstructor
public class LoginVO {
    private String token;
    /**
     * 后端 users 表中的主键ID（32位UUID）
     */
    private String userId;
    private String username;
    private String role; // 用户角色：ADMIN / INSPECTOR / USER

    public LoginVO(String token, String userId, String username, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }
}

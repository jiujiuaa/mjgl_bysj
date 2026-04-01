package com.zjb.mjgl.pojo.dto;

import lombok.Data;

/**
 * 当前登录用户个人资料更新 DTO
 */
@Data
public class UserProfileUpdateDTO {

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 新密码（可选）
     */
    private String newPassword;
}

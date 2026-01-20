package com.zjb.mjgl.pojo.dto;

import lombok.Data;

/**
 * 注册请求 DTO
 */
@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String realName;
    private Integer age;
    private String phone;
    private String email;
    private String role;
}

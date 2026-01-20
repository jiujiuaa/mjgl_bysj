package com.zjb.mjgl.pojo.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjb.mjgl.common.enums.RoleEnum;
import com.zjb.mjgl.common.enums.UserStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    private String id;
    private String username;
    private String realName;
    private Integer age;
    private String phone;
    private String email;
    private RoleEnum role;

    // 前端可直接显示 status.code (0/1) 或 status.description ("启用"/"禁用")
    private UserStatusEnum status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
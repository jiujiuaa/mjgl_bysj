package com.zjb.mjgl.pojo.entity;



import com.zjb.mjgl.common.enums.RoleEnum;
import com.zjb.mjgl.common.enums.UserStatusEnum;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    private String id; // 32位UUID字符串

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private UserStatusEnum status = UserStatusEnum.ENABLED;

    @Column(name = "real_name", nullable = false)
    private String realName;

    private Integer age;
    private String phone;
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleEnum role = RoleEnum.USER;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public boolean isEnabled() {
        if(status == UserStatusEnum.ENABLED) {
            return true;
        }
        else return false;
    }

    // Getters and Setters
}
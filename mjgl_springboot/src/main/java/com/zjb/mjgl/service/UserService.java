package com.zjb.mjgl.service;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.RegisterDTO;
import com.zjb.mjgl.pojo.dto.UserQueryDTO;
import com.zjb.mjgl.pojo.vo.UserVO;

import java.util.List;

/**
 * 用户相关业务接口
 */
public interface UserService {
    void registerUser(RegisterDTO registerDTO);
    Result<List<UserVO>> getAllUsers();
    Result<String> updateUser(String id, RegisterDTO updateDTO);
    Result<String> deleteUser(String id);
    Result<String> updateUserStatus(String id, String status);
    Result<List<UserVO>> queryUsers(UserQueryDTO queryDTO);
}

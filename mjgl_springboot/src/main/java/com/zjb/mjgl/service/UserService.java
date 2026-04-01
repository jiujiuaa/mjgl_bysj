package com.zjb.mjgl.service;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.pojo.dto.RegisterDTO;
import com.zjb.mjgl.pojo.dto.UserProfileUpdateDTO;
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

    Result<String> deleteUsersBatch(List<String> ids);

    Result<String> updateUserStatus(String id, String status);

    Result<List<UserVO>> queryUsers(UserQueryDTO queryDTO);

    /**
     * 获取当前登录用户个人资料
     */
    Result<UserVO> getCurrentUserProfile();

    /**
     * 更新当前登录用户个人资料
     */
    Result<String> updateCurrentUserProfile(UserProfileUpdateDTO profileUpdateDTO);
}

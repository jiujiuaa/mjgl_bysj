package com.zjb.mjgl.service.impl;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.common.enums.RoleEnum;
import com.zjb.mjgl.common.enums.UserStatusEnum;
import com.zjb.mjgl.mapper.UserMapper;
import com.zjb.mjgl.pojo.dto.BatchIdsDTO;
import com.zjb.mjgl.pojo.dto.RegisterDTO;
import com.zjb.mjgl.pojo.dto.UserProfileUpdateDTO;
import com.zjb.mjgl.pojo.dto.UserQueryDTO;
import com.zjb.mjgl.pojo.entity.User;
import com.zjb.mjgl.pojo.vo.UserVO;
import com.zjb.mjgl.service.UserService;
import com.zjb.mjgl.utils.IdUtil;
import com.zjb.mjgl.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户业务实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void registerUser(RegisterDTO registerDTO) {
        // 1. 校验用户名唯一性
        if (userMapper.selectOneByusername(registerDTO.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 2. 校验并转换角色
        RoleEnum role;
        try {
            role = RoleEnum.valueOf(registerDTO.getRole().trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的角色");
        }

        // 3. 手动构建 User 对象
        User user = new User();
        user.setId(IdUtil.fastUUID());
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword())); // 🔒 必须加密！
        user.setRealName(registerDTO.getRealName());
        user.setAge(registerDTO.getAge());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setRole(role);
        user.setStatus(UserStatusEnum.ENABLED); // 默认启用
        user.setCreatedAt(LocalDateTime.now());

        // 4. 保存到数据库
        userMapper.insertUser(user);
        log.info("注册成功，用户username为 {}", user.getUsername());
    }

    @Override
    public Result<List<UserVO>> getAllUsers() {
        // 查询所有用户实体
        List<User> allUsers = userMapper.getAllUsers();

        // 使用 Lambda + Stream 将实体转换为 VO
        List<UserVO> userVOList = allUsers.stream().map(this::convertToVO).collect(Collectors.toList());

        return Result.success(userVOList);
    }
    
    /**
     * 将 User 实体转换为 UserVO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setAge(user.getAge());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }

    @Override
    public Result<String> updateUser(String id, RegisterDTO updateDTO) {
        User existingUser = userMapper.selectOneById(id);
        if (existingUser == null) {
            return Result.fail("用户不存在");
        }

        if (updateDTO.getRealName() != null && !updateDTO.getRealName().trim().isEmpty()) {
            existingUser.setRealName(updateDTO.getRealName());
        }
        if (updateDTO.getAge() != null) {
            existingUser.setAge(updateDTO.getAge());
        }
        if (updateDTO.getPhone() != null) {
            existingUser.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getEmail() != null) {
            existingUser.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getRole() != null && !updateDTO.getRole().trim().isEmpty()) {
            try {
                existingUser.setRole(RoleEnum.valueOf(updateDTO.getRole().trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                return Result.fail("无效的角色");
            }
        }
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }

        int rows = userMapper.updateUser(existingUser);
        if (rows > 0) {
            log.info("更新用户成功，用户id为 {}", id);
            return Result.success("更新用户成功");
        }
        return Result.fail("更新用户失败");
    }

    @Override
    public Result<String> deleteUser(String id) {
        User user = userMapper.selectOneById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        int rows = userMapper.deleteUserById(id);
        if (rows > 0) {
            log.info("删除用户成功，用户id为 {}", id);
            return Result.success("删除用户成功");
        }
        return Result.fail("删除用户失败");
    }

    @Override
    public Result<String> deleteUsersBatch(List<String> rawIds) {
        List<String> ids = BatchIdsDTO.normalizeList(rawIds);
        if (ids.isEmpty()) {
            return Result.fail("请选择要删除的用户");
        }
        return ids.stream()
                .map(this::deleteUser)
                .filter(r -> r.getCode() != 200)
                .findFirst()
                .orElseGet(() -> Result.success("已删除 " + ids.size() + " 个用户"));
    }

    @Override
    public Result<String> updateUserStatus(String id, String status) {
        User user = userMapper.selectOneById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        try {
            UserStatusEnum statusEnum = UserStatusEnum.valueOf(status.trim().toUpperCase());
            user.setStatus(statusEnum);
            int rows = userMapper.updateUser(user);
            if (rows > 0) {
                log.info("更新用户状态成功，用户id为 {}，状态为 {}", id, status);
                return Result.success("更新用户状态成功");
            }
            return Result.fail("更新用户状态失败");
        } catch (IllegalArgumentException e) {
            return Result.fail("无效的状态");
        }
    }

    @Override
    public Result<List<UserVO>> queryUsers(UserQueryDTO queryDTO) {
        // 直接传递 DTO，由前端传 enabled (0/1)
        List<User> users = userMapper.queryUsers(queryDTO);

        // 使用 Lambda + Stream 转换为 VO
        List<UserVO> userVOList = users.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());

        return Result.success(userVOList);
    }

    @Override
    public Result<UserVO> getCurrentUserProfile() {
        User currentUser = UserUtils.getCurrentUserDetails();
        if (currentUser == null) {
            return Result.fail("未登录或登录已失效");
        }
        User freshUser = userMapper.selectOneById(currentUser.getId());
        if (freshUser == null) {
            return Result.fail("用户不存在");
        }
        return Result.success(convertToVO(freshUser));
    }

    @Override
    public Result<String> updateCurrentUserProfile(UserProfileUpdateDTO profileUpdateDTO) {
        User currentUser = UserUtils.getCurrentUserDetails();
        if (currentUser == null) {
            return Result.fail("未登录或登录已失效");
        }
        User existingUser = userMapper.selectOneById(currentUser.getId());
        if (existingUser == null) {
            return Result.fail("用户不存在");
        }

        if (profileUpdateDTO.getRealName() != null && !profileUpdateDTO.getRealName().trim().isEmpty()) {
            existingUser.setRealName(profileUpdateDTO.getRealName());
        }
        if (profileUpdateDTO.getAge() != null) {
            existingUser.setAge(profileUpdateDTO.getAge());
        }
        if (profileUpdateDTO.getPhone() != null) {
            existingUser.setPhone(profileUpdateDTO.getPhone());
        }
        if (profileUpdateDTO.getEmail() != null) {
            existingUser.setEmail(profileUpdateDTO.getEmail());
        }
        if (profileUpdateDTO.getNewPassword() != null && !profileUpdateDTO.getNewPassword().trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(profileUpdateDTO.getNewPassword()));
        }

        int rows = userMapper.updateUser(existingUser);
        if (rows > 0) {
            log.info("更新个人资料成功，用户id为 {}", existingUser.getId());
            return Result.success("更新个人资料成功");
        }
        return Result.fail("更新个人资料失败");
    }
}


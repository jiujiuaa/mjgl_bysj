package com.zjb.mjgl.controller;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.common.UserDetailsImpl;
import com.zjb.mjgl.pojo.dto.LoginDTO;
import com.zjb.mjgl.pojo.dto.RegisterDTO;
import com.zjb.mjgl.pojo.dto.UserQueryDTO;
import com.zjb.mjgl.pojo.vo.LoginVO;
import com.zjb.mjgl.pojo.vo.UserVO;
import com.zjb.mjgl.service.UserService;
import com.zjb.mjgl.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        try {
            // 创建认证token
            UsernamePasswordAuthenticationToken authenticationToken = 
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(),
                        loginDTO.getPassword()
                );

            // 进行认证
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            log.info(authentication.toString());
            
            // 认证成功后，获取UserDetails
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 生成JWT token
            String token = jwtUtil.generateToken(userDetails);

            // 获取用户角色
            String role = userDetails.getUser().getRole().name(); // ADMIN / INSPECTOR / USER
            log.info(role);
            // 构建响应 VO（包含角色信息）
            LoginVO loginVO = new LoginVO(token, userDetails.getUsername(), role);

            return Result.success(loginVO);
        } catch (BadCredentialsException e) {
            return Result.fail("用户名或密码错误");
        } catch (Exception e) {
            return Result.fail("登录失败: " + e.getMessage());
        }
    }

    /**
     * 退出登录（基于 JWT，为无状态退出：前端删除 token 即可）
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        // 这里不需要做任何服务器端状态清理，直接返回成功即可
        return Result.success();
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> register(@RequestBody RegisterDTO registerDTO) {
        try {
            // 验证必填字段
            if (registerDTO.getUsername() == null || registerDTO.getUsername().trim().isEmpty()) {
                return Result.fail("用户名不能为空");
            }
            if (registerDTO.getPassword() == null || registerDTO.getPassword().trim().isEmpty()) {
                return Result.fail("密码不能为空");
            }
            if (registerDTO.getRealName() == null || registerDTO.getRealName().trim().isEmpty()) {
                return Result.fail("真实姓名不能为空");
            }

            // 注册用户
            userService.registerUser(registerDTO);
            
            return Result.success();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("注册失败", e);
            return Result.fail("注册失败: " + e.getMessage());
        }
    }

    @GetMapping("/alluser")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<UserVO>> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * 更新用户信息
     * @param id 用户ID
     * @param updateDTO 更新数据（复用 RegisterDTO，password 可选，username 不更新）
     * @return 操作结果
     */
    @PutMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateUser(@PathVariable String id, @RequestBody RegisterDTO updateDTO) {
        try {
            return userService.updateUser(id, updateDTO);
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return Result.fail("更新用户失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> deleteUser(@PathVariable String id) {
        try {
            return userService.deleteUser(id);
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return Result.fail("删除用户失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用用户账号
     * @param id 用户ID
     * @param requestBody 请求体，包含 status 字段（"ENABLED" 或 "DISABLED"）
     * @return 操作结果
     */
    @PutMapping("/user/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> updateUserStatus(@PathVariable String id, @RequestBody Map<String, String> requestBody) {
        try {
            String status = requestBody.get("status");
            if (status == null || status.trim().isEmpty()) {
                return Result.fail("状态不能为空");
            }
            return userService.updateUserStatus(id, status);
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            return Result.fail("更新用户状态失败: " + e.getMessage());
        }
    }

    /**
     * 根据条件查询用户
     * @param queryDTO 查询条件 DTO
     * @return 符合条件的用户列表
     */
    @GetMapping("/users/query")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<UserVO>> queryUsers(UserQueryDTO queryDTO) {
        try {
            return userService.queryUsers(queryDTO);
        } catch (Exception e) {
            log.error("查询用户失败", e);
            return Result.fail("查询用户失败: " + e.getMessage());
        }
    }

}

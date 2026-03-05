/*package com.zjb.mjgl.controller;

import com.zjb.mjgl.common.Result;
import com.zjb.mjgl.common.UserDetailsImpl;
import com.zjb.mjgl.mapper.UserMapper;
import com.zjb.mjgl.pojo.dto.LoginDTO;
import com.zjb.mjgl.pojo.entity.User;
import com.zjb.mjgl.pojo.vo.LoginVO;
import com.zjb.mjgl.utils.JwtUtil;
import com.zjb.mjgl.utils.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserMapper userMapper;

    @PostMapping("/loginnn")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        try {
            // 创建认证token
            UsernamePasswordAuthenticationToken authenticationToken = 
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(),
                        loginDTO.getPassword()
                );
           // String encodedPassword = new BCryptPasswordEncoder().encode("123456");
            //log.info(encodedPassword);
            // 进行认证
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            log.info(authentication.toString());
            // 认证成功后，获取UserDetails
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 生成JWT token
            String token = jwtUtil.generateToken(userDetails);
            User user = userMapper.selectOneByusername(userDetails.getUsername());
            // 构建响应 VO
            LoginVO loginVO = new LoginVO(token, userDetails.getUsername(),user.getRole().toString());

            return Result.success(loginVO);
        } catch (BadCredentialsException e) {
            return Result.fail("用户名或密码错误");
        } catch (Exception e) {
            return Result.fail("登录失败: " + e.getMessage());
        }
    }
    @PreAuthorize("hasRole('INSPECTOR')")
    @PostMapping("/check-login")
    public Result<?> checkLogin() {
        User user = UserUtils.getCurrentUserDetails();
        if (user == null) {
            return Result.fail("未登录");
        }
        log.info(user.toString());
        return Result.success(); // 👈 只返回成功，data 为 null 或空
    }
}
*/
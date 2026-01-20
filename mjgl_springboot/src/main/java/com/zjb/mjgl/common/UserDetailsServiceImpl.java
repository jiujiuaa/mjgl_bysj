package com.zjb.mjgl.common;

import com.zjb.mjgl.mapper.UserMapper;
import com.zjb.mjgl.pojo.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userMapper.selectOneByusername(username);
        List<String> authorities = userMapper.findAuthoritiesByRole(user.getRole().getValue());
        if (authorities == null || authorities.isEmpty()) {
            // 可选：给默认角色，避免空权限
            authorities = Collections.singletonList("ROLE_USER");
        }
        log.info(user.toString());
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return new UserDetailsImpl(user,authorities);	// UserDetailsImpl 是我们实现的类
    }


}
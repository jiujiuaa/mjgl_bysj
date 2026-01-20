package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.dto.UserQueryDTO;
import com.zjb.mjgl.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    User selectOneByusername(String username);
    User selectOneById(String id);
    int insertUser(User user);
    int updateUser(User user);
    int deleteUserById(String id);
    List<String> findAuthoritiesByRole(String role);
    List<User> getAllUsers();
    List<User> queryUsers(@Param("query") UserQueryDTO queryDTO);
}

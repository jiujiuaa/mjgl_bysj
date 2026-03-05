package com.zjb.mjgl.mapper;

import com.zjb.mjgl.pojo.entity.UserNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserNotificationMapper {

    int insert(UserNotification notification);

    List<UserNotification> selectUnreadByUserId(@Param("userId") String userId);

    List<UserNotification> selectAllByUserId(@Param("userId") String userId);

    int markRead(@Param("id") String id);
}


package com.repair.mapper;

import org.apache.ibatis.annotations.*;

@Mapper
public interface AnnouncementReadMapper {

    // 幂等插入：重复标记已读不报错
    @Insert("INSERT IGNORE INTO announcement_read(announcement_id, user_id) VALUES(#{announcementId}, #{userId})")
    int insert(@Param("announcementId") Integer announcementId, @Param("userId") Integer userId);

    @Select("SELECT COUNT(*) FROM announcement_read WHERE announcement_id = #{announcementId} AND user_id = #{userId}")
    int exists(@Param("announcementId") Integer announcementId, @Param("userId") Integer userId);

    // 已读的启用中公告数
    @Select("SELECT COUNT(*) FROM announcement_read ar JOIN announcement a ON ar.announcement_id = a.id " +
            "WHERE ar.user_id = #{userId} AND a.status = 1")
    Long countReadEnabledByUserId(Integer userId);
}
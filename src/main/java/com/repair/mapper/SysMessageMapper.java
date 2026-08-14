package com.repair.mapper;

import com.repair.entity.SysMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysMessageMapper {

    @Insert("INSERT INTO sys_message(receiver_id, type, title, content, order_id, is_read) " +
            "VALUES(#{receiverId}, #{type}, #{title}, #{content}, #{orderId}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysMessage message);

    @Select("SELECT * FROM sys_message WHERE id = #{id}")
    SysMessage selectById(Integer id);

    @Select("<script>SELECT * FROM sys_message WHERE receiver_id = #{receiverId} " +
            "<if test='isRead != null'>AND is_read = #{isRead}</if> " +
            "ORDER BY id DESC LIMIT #{offset}, #{pageSize}</script>")
    List<SysMessage> selectPage(@Param("receiverId") Integer receiverId, @Param("isRead") Integer isRead,
                                @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>SELECT COUNT(*) FROM sys_message WHERE receiver_id = #{receiverId} " +
            "<if test='isRead != null'>AND is_read = #{isRead}</if></script>")
    Long countPage(@Param("receiverId") Integer receiverId, @Param("isRead") Integer isRead);

    @Select("SELECT COUNT(*) FROM sys_message WHERE receiver_id = #{receiverId} AND is_read = 0")
    Long countUnread(Integer receiverId);

    @Update("UPDATE sys_message SET is_read = 1, read_at = NOW() WHERE id = #{id} AND receiver_id = #{receiverId}")
    int updateRead(@Param("id") Integer id, @Param("receiverId") Integer receiverId);

    @Update("UPDATE sys_message SET is_read = 1, read_at = NOW() WHERE receiver_id = #{receiverId} AND is_read = 0")
    int markAllRead(Integer receiverId);

    @Delete("DELETE FROM sys_message WHERE id = #{id}")
    int deleteById(Integer id);

    @Select("SELECT COUNT(*) FROM sys_message WHERE order_id = #{orderId} AND type = #{type} AND receiver_id = #{receiverId}")
    Long countByOrderIdAndType(@Param("orderId") Integer orderId, @Param("type") Integer type,
                               @Param("receiverId") Integer receiverId);
}
package com.repair.mapper;

import com.repair.entity.Announcement;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnnouncementMapper {

    @Insert("INSERT INTO announcement(title, content, type, is_top, status) " +
            "VALUES(#{title}, #{content}, #{type}, #{isTop}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Announcement announcement);

    @Delete("DELETE FROM announcement WHERE id = #{id}")
    int deleteById(Integer id);

    @Update("UPDATE announcement SET title=#{title}, content=#{content}, type=#{type}, " +
            "is_top=#{isTop}, status=#{status} WHERE id=#{id}")
    int updateById(Announcement announcement);

    @Select("SELECT * FROM announcement WHERE id = #{id}")
    Announcement selectById(Integer id);

    @Select("SELECT * FROM announcement ORDER BY is_top DESC, created_at DESC")
    List<Announcement> selectAll();

    @Select("SELECT * FROM announcement WHERE status = 1 ORDER BY is_top DESC, created_at DESC")
    List<Announcement> selectPublished();
}
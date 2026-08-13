package com.repair.mapper;

import com.repair.entity.Feedback;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FeedbackMapper {

    @Insert("INSERT INTO feedback(owner_id, title, content, type, status) " +
            "VALUES(#{ownerId}, #{title}, #{content}, #{type}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Feedback feedback);

    @Select("SELECT * FROM feedback WHERE id = #{id}")
    Feedback selectById(Integer id);

    @Select("SELECT * FROM feedback WHERE owner_id = #{ownerId} ORDER BY created_at DESC")
    List<Feedback> selectByOwnerId(Integer ownerId);

    @Select("SELECT * FROM feedback ORDER BY status ASC, created_at DESC")
    List<Feedback> selectAll();

    @Update("UPDATE feedback SET reply=#{reply}, status=1 WHERE id=#{id}")
    int reply(@Param("id") Integer id, @Param("reply") String reply);

    @Delete("DELETE FROM feedback WHERE id = #{id}")
    int deleteById(Integer id);
}
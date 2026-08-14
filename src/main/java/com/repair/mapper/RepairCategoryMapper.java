package com.repair.mapper;

import com.repair.entity.RepairCategory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RepairCategoryMapper {

    @Insert("INSERT INTO repair_category(name, sort_order, status) VALUES(#{name}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RepairCategory category);

    @Delete("DELETE FROM repair_category WHERE id = #{id}")
    int deleteById(Integer id);

    @Update("UPDATE repair_category SET name=#{name}, sort_order=#{sortOrder}, status=#{status} WHERE id=#{id}")
    int updateById(RepairCategory category);

    @Select("SELECT * FROM repair_category WHERE id = #{id}")
    RepairCategory selectById(Integer id);

    @Select("SELECT * FROM repair_category WHERE name = #{name}")
    RepairCategory selectByName(String name);

    @Select("SELECT * FROM repair_category ORDER BY sort_order ASC, id ASC")
    List<RepairCategory> selectAll();

    @Select("SELECT * FROM repair_category WHERE status = 1 ORDER BY sort_order ASC, id ASC")
    List<RepairCategory> selectEnabled();
}
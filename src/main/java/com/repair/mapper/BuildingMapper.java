package com.repair.mapper;

import com.repair.entity.Building;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BuildingMapper {

    @Insert("INSERT INTO building(name, sort_order, status) VALUES(#{name}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Building building);

    @Delete("DELETE FROM building WHERE id = #{id}")
    int deleteById(Integer id);

    @Update("UPDATE building SET name=#{name}, sort_order=#{sortOrder}, status=#{status} WHERE id=#{id}")
    int updateById(Building building);

    @Select("SELECT * FROM building WHERE id = #{id}")
    Building selectById(Integer id);

    @Select("SELECT * FROM building WHERE name = #{name}")
    Building selectByName(String name);

    @Select("SELECT * FROM building ORDER BY sort_order ASC, id ASC")
    List<Building> selectAll();

    @Select("SELECT * FROM building WHERE status = 1 ORDER BY sort_order ASC, id ASC")
    List<Building> selectEnabled();
}
package com.repair.mapper;

import com.repair.entity.BuildingUnit;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BuildingUnitMapper {

    @Insert("INSERT INTO unit_info(building_id, name, sort_order, status) VALUES(#{buildingId}, #{name}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BuildingUnit unit);

    @Delete("DELETE FROM unit_info WHERE id = #{id}")
    int deleteById(Integer id);

    @Update("UPDATE unit_info SET building_id=#{buildingId}, name=#{name}, sort_order=#{sortOrder}, status=#{status} WHERE id=#{id}")
    int updateById(BuildingUnit unit);

    @Select("SELECT u.*, b.name AS building_name FROM unit_info u LEFT JOIN building b ON u.building_id = b.id WHERE u.id = #{id}")
    BuildingUnit selectById(Integer id);

    @Select("SELECT * FROM unit_info WHERE building_id = #{buildingId} AND name = #{name}")
    BuildingUnit selectByBuildingIdAndName(@Param("buildingId") Integer buildingId, @Param("name") String name);

    @Select("SELECT u.*, b.name AS building_name FROM unit_info u LEFT JOIN building b ON u.building_id = b.id WHERE u.building_id = #{buildingId} ORDER BY u.sort_order ASC, u.id ASC")
    List<BuildingUnit> selectByBuildingId(Integer buildingId);

    @Select("SELECT u.*, b.name AS building_name FROM unit_info u LEFT JOIN building b ON u.building_id = b.id ORDER BY b.sort_order ASC, u.sort_order ASC, u.id ASC")
    List<BuildingUnit> selectAll();

    @Select("SELECT COUNT(*) FROM unit_info WHERE building_id = #{buildingId}")
    int countByBuildingId(Integer buildingId);
}
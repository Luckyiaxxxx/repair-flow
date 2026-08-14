package com.repair.mapper;

import com.repair.entity.SysDict;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysDictMapper {

    @Insert("INSERT INTO sys_dict(dict_type, dict_label, dict_value, sort_order, status, remark) " +
            "VALUES(#{dictType}, #{dictLabel}, #{dictValue}, #{sortOrder}, #{status}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysDict dict);

    @Delete("DELETE FROM sys_dict WHERE id = #{id}")
    int deleteById(Integer id);

    @Update("UPDATE sys_dict SET dict_type=#{dictType}, dict_label=#{dictLabel}, dict_value=#{dictValue}, " +
            "sort_order=#{sortOrder}, status=#{status}, remark=#{remark} WHERE id=#{id}")
    int updateById(SysDict dict);

    @Select("SELECT * FROM sys_dict WHERE id = #{id}")
    SysDict selectById(Integer id);

    @Select("SELECT * FROM sys_dict WHERE dict_type = #{dictType} AND dict_value = #{dictValue}")
    SysDict selectByTypeAndValue(@Param("dictType") String dictType, @Param("dictValue") String dictValue);

    @Select("SELECT * FROM sys_dict WHERE dict_type = #{dictType} ORDER BY sort_order ASC, id ASC")
    List<SysDict> selectByType(String dictType);

    @Select("SELECT * FROM sys_dict WHERE dict_type = #{dictType} AND status = 1 ORDER BY sort_order ASC, id ASC")
    List<SysDict> selectEnabledByType(String dictType);

    @Select("SELECT * FROM sys_dict ORDER BY dict_type ASC, sort_order ASC, id ASC")
    List<SysDict> selectAll();

    @Select("SELECT DISTINCT dict_type FROM sys_dict ORDER BY dict_type ASC")
    List<String> selectAllTypes();
}
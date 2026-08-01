package com.repair.mapper;

import com.repair.entity.Material;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MaterialMapper {

    // 新增物料
    @Insert("INSERT INTO material(name, category, spec, unit, stock, min_stock, price, supplier) " +
            "VALUES(#{name}, #{category}, #{spec}, #{unit}, #{stock}, #{minStock}, #{price}, #{supplier})")
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insert(Material material);

    //根据 ID 删除物料
    @Delete("DELETE FROM material WHERE id = #{id}")
    int deletById(Integer id);

    //更新物料
    @Update("UPDATE material SET name=#{name}, category=#{category}, spec=#{spec}, unit=#{unit}, " +
            "stock=#{stock}, min_stock=#{minStock}, price=#{price}, supplier=#{supplier} WHERE id=#{id}")
    int updateById(Material material);

    //扣减库存
    @Update("UPDATE material SET stock = stock - #{quantity} WHERE id = #{id} AND stock >= #{quantity}")
    int deductStock(@Param("id") Integer id, @Param("quantity") Integer quantity);

    //增加库存
    @Update("UPDATE material SET stock = stock + #{quantity} WHERE id = #{id}")
    int addStock(@Param("id") Integer id, @Param("quantity") Integer quantity);

    //根据 ID 查询物料
    @Select("SELECT * FROM material WHERE id = #{id}")
    Material selectById(Integer id);

    //查询库存不足的物料
    @Select("SELECT * FROM material WHERE stock < min_stock")
    List<Material> selectLowStock();

    //查询所有物料
    @Select("SELECT * FROM material")
    List<Material> selectAll();
}
package com.repair.mapper;

import com.repair.entity.MaterialConsumption;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MaterialConsumptionMapper {

    @Insert("INSERT INTO material_consumption(order_id, material_id, quantity) VALUES(#{orderId}, #{materialId}, #{quantity})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MaterialConsumption consumption);

    @Select("SELECT * FROM material_consumption WHERE order_id = #{orderId}")
    List<MaterialConsumption> selectByOrderId(Integer orderId);
}
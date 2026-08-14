package com.repair.mapper;

import com.repair.entity.OrderReassignLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderReassignLogMapper {

    @Insert("INSERT INTO order_reassign_log(order_id, old_worker_id, new_worker_id, dispatcher_id, source) " +
            "VALUES(#{orderId}, #{oldWorkerId}, #{newWorkerId}, #{dispatcherId}, #{source})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderReassignLog log);

    @Select("SELECT * FROM order_reassign_log WHERE order_id = #{orderId} ORDER BY created_at DESC")
    List<OrderReassignLog> selectByOrderId(Integer orderId);
}
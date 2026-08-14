package com.repair.mapper;

import com.repair.entity.OrderMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMessageMapper {

    @Insert("INSERT INTO order_message(order_id, sender_id, sender_role, content) " +
            "VALUES(#{orderId}, #{senderId}, #{senderRole}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderMessage message);

    @Select("SELECT * FROM order_message WHERE order_id = #{orderId} ORDER BY created_at ASC, id ASC")
    List<OrderMessage> selectByOrderId(Integer orderId);
}
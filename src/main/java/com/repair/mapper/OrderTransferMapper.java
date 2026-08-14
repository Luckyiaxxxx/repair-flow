package com.repair.mapper;

import com.repair.entity.OrderTransfer;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderTransferMapper {

    @Insert("INSERT INTO order_transfer(order_id, from_worker_id, type, reason, status) " +
            "VALUES(#{orderId}, #{fromWorkerId}, #{type}, #{reason}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderTransfer transfer);

    @Select("SELECT * FROM order_transfer WHERE id = #{id}")
    OrderTransfer selectById(Integer id);

    @Select("SELECT * FROM order_transfer WHERE order_id = #{orderId} AND status = 0 ORDER BY created_at DESC")
    List<OrderTransfer> selectPendingByOrderId(Integer orderId);

    @Select("SELECT * FROM order_transfer WHERE status = 0 ORDER BY created_at ASC")
    List<OrderTransfer> selectPendingAll();

    @Select("SELECT * FROM order_transfer WHERE from_worker_id = #{workerId} ORDER BY created_at DESC")
    List<OrderTransfer> selectByWorkerId(Integer workerId);

    /**
     * 处理申请：同意(1)或拒绝(2)
     */
    @Update("UPDATE order_transfer SET status = #{status}, dispatcher_id = #{dispatcherId}, " +
            "to_worker_id = #{toWorkerId}, handle_note = #{handleNote}, handled_at = NOW() WHERE id = #{id}")
    int updateHandle(@Param("id") Integer id, @Param("status") Integer status,
                     @Param("dispatcherId") Integer dispatcherId, @Param("toWorkerId") Integer toWorkerId,
                     @Param("handleNote") String handleNote);
}
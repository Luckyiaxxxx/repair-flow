package com.repair.mapper;

import com.repair.entity.RepairOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RepairOrderMapper {
    @Insert("INSERT INTO repair_order(order_no, owner_id, building, unit, room, category, description, images, " +
            "emergency_level, status, dispatcher_id, worker_id, assigned_at, accepted_at, " +
            "completed_at, repair_duration, repair_note, need_revisit, timeout_at, is_timeout) " +
            "VALUES(#{orderNo}, #{ownerId}, #{building}, #{unit}, #{room}, #{category}, #{description}, #{images}, " +
            "#{emergencyLevel}, #{status}, #{dispatcherId}, #{workerId}, #{assignedAt}, #{acceptedAt}, " +
            "#{completedAt}, #{repairDuration}, #{repairNote}, #{needRevisit}, #{timeoutAt}, #{isTimeout})")
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insert(RepairOrder order);

    @Delete("DELETE FROM repair_order WHERE id = #{id}")
    int deletById(Integer id);

    @Update("UPDATE repair_order SET status=#{status}, worker_id=#{workerId}, assigned_at=#{assignedAt}, " +
            "accepted_at=#{acceptedAt}, completed_at=#{completedAt}, repair_duration=#{repairDuration}, " +
            "repair_note=#{repairNote}, need_revisit=#{needRevisit}, is_timeout=#{isTimeout} WHERE id=#{id}")
    int updateById(RepairOrder order);

    @Select("SELECT * FROM repair_order WHERE id= #{id}")
    RepairOrder selectById(Integer id);

    @Select("SELECT * FROM repair_order WHERE owner_id = #{ownerId} ORDER BY created_at DESC")
    List<RepairOrder> selectByOwnerId(Integer workerId);

    @Select("SELECT * FROM repair_order WHERE worker_id = #{workerId} ORDER BY created_at DESC")
    List<RepairOrder> selectByWorkerId(Integer workerId);

    @Select("SELECT * FROM repair_order WHERE status = #{status}")
    List<RepairOrder> selectByStatus(Integer status);

    @Select("SELECT * FROM repair_order WHERE status = 1 OREDER BY emergency_level DESC,created_at ASC")
    List<RepairOrder> selectPendingOrders();

    @Select("SELECT * FROM repair_order")
    List<RepairOrder> selectAll();
}
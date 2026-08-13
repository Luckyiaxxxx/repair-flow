package com.repair.mapper;

import com.repair.entity.RepairOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface RepairOrderMapper {

    @Insert("INSERT INTO repair_order(order_no, owner_id, building, unit, room, category, description, images, " +
            "emergency_level, status, dispatcher_id, worker_id, assigned_at, accepted_at, " +
            "completed_at, repair_duration, repair_note, labor_cost, material_cost, need_revisit, timeout_at, is_timeout) " +
            "VALUES(#{orderNo}, #{ownerId}, #{building}, #{unit}, #{room}, #{category}, #{description}, #{images}, " +
            "#{emergencyLevel}, #{status}, #{dispatcherId}, #{workerId}, #{assignedAt}, #{acceptedAt}, " +
            "#{completedAt}, #{repairDuration}, #{repairNote}, #{laborCost}, #{materialCost}, #{needRevisit}, #{timeoutAt}, #{isTimeout})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RepairOrder order);

    @Delete("DELETE FROM repair_order WHERE id = #{id}")
    int deleteById(Integer id);

    @Update("UPDATE repair_order SET status=#{status}, worker_id=#{workerId}, assigned_at=#{assignedAt}, " +
            "accepted_at=#{acceptedAt}, completed_at=#{completedAt}, repair_duration=#{repairDuration}, " +
            "repair_note=#{repairNote}, labor_cost=#{laborCost}, material_cost=#{materialCost}, " +
            "need_revisit=#{needRevisit}, is_timeout=#{isTimeout} WHERE id=#{id}")
    int updateById(RepairOrder order);

    @Select("SELECT * FROM repair_order WHERE id = #{id}")
    RepairOrder selectById(Integer id);

    @Select("SELECT * FROM repair_order WHERE owner_id = #{ownerId} ORDER BY created_at DESC")
    List<RepairOrder> selectByOwnerId(Integer ownerId);

    @Select("SELECT * FROM repair_order WHERE worker_id = #{workerId} ORDER BY created_at DESC")
    List<RepairOrder> selectByWorkerId(Integer workerId);

    @Select("SELECT * FROM repair_order WHERE status = #{status}")
    List<RepairOrder> selectByStatus(Integer status);

    @Select("SELECT * FROM repair_order WHERE status = 1 ORDER BY emergency_level DESC, created_at ASC")
    List<RepairOrder> selectPendingOrders();

    @Select("SELECT * FROM repair_order")
    List<RepairOrder> selectAll();

    /**
     * 统计维修工的总完工数
     */
    @Select("SELECT COUNT(*) FROM repair_order WHERE worker_id = #{workerId} AND status = 4")
    Integer countCompletedByWorkerId(Integer workerId);

    // ===== 看板统计 =====

    @Select("SELECT COUNT(*) FROM repair_order")
    Long countAll();

    @Select("SELECT COUNT(*) FROM repair_order WHERE status = #{status}")
    Long countByStatus(Integer status);

    @Select("SELECT status, COUNT(*) as count FROM repair_order GROUP BY status")
    List<Map<String, Object>> countByStatusGroup();

    @Select("SELECT category, COUNT(*) as count FROM repair_order GROUP BY category")
    List<Map<String, Object>> countByCategory();

    @Select("SELECT DATE(created_at) as date, COUNT(*) as count " +
            "FROM repair_order " +
            "WHERE created_at >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date ASC")
    List<Map<String, Object>> selectTrend(@Param("days") int days);
}

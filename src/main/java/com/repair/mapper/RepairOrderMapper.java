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
     * 查询指定前缀的最大工单编号（用于生成每日自增序号）
     */
    @Select("SELECT MAX(order_no) FROM repair_order WHERE order_no LIKE CONCAT(#{prefix}, '%')")
    String selectMaxOrderNoByPrefix(@Param("prefix") String prefix);

    /**
     * 统计维修工的总完工数
     */
    @Select("SELECT COUNT(*) FROM repair_order WHERE worker_id = #{workerId} AND status = 4")
    Integer countCompletedByWorkerId(Integer workerId);

    /**
     * 统计维修工当前负荷：已派单(2)或维修中(3)的工单数
     */
    @Select("SELECT COUNT(*) FROM repair_order WHERE worker_id = #{workerId} AND status IN (2,3)")
    Integer countActiveByWorkerId(Integer workerId);

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
    @Select("<script>" +
            "SELECT * FROM repair_order WHERE 1=1 " +
            "<if test='status != null'> AND status = #{status}</if> " +
            "<if test='category != null and category != \"\"'> AND category = #{category}</if> " +
            "<if test='building != null and building != \"\"'> AND building LIKE CONCAT('%', #{building}, '%')</if> " +
            "<if test='startDate != null and startDate != \"\"'> AND created_at &gt;= #{startDate}</if> " +
            "<if test='endDate != null and endDate != \"\"'> AND created_at &lt;= #{endDate}</if> " +
            "ORDER BY emergency_level DESC, created_at ASC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<RepairOrder> search(@Param("status") Integer status,
                             @Param("category") String category,
                             @Param("building") String building,
                             @Param("startDate") String startDate,
                             @Param("endDate") String endDate,
                             @Param("offset") int offset,
                             @Param("pageSize") int pageSize);

    @Select("<script>" +
            "SELECT COUNT(*) FROM repair_order WHERE 1=1 " +
            "<if test='status != null'> AND status = #{status}</if> " +
            "<if test='category != null and category != \"\"'> AND category = #{category}</if> " +
            "<if test='building != null and building != \"\"'> AND building LIKE CONCAT('%', #{building}, '%')</if> " +
            "<if test='startDate != null and startDate != \"\"'> AND created_at &gt;= #{startDate}</if> " +
            "<if test='endDate != null and endDate != \"\"'> AND created_at &lt;= #{endDate}</if> " +
            "</script>")
    Long countSearch(@Param("status") Integer status,
                     @Param("category") String category,
                     @Param("building") String building,
                     @Param("startDate") String startDate,
                     @Param("endDate") String endDate);
}

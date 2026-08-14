package com.repair.mapper;

import com.repair.entity.RepairOrder;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface RepairOrderMapper {

    @Insert("INSERT INTO repair_order(order_no, owner_id, building, unit, room, category, description, images, " +
            "emergency_level, status, dispatcher_id, worker_id, assigned_at, accepted_at, " +
            "completed_at, repair_duration, repair_note, labor_cost, material_cost, need_revisit, timeout_at, is_timeout, " +
            "urge_count, last_urge_at, close_reason, closed_by, closed_at, " +
            "preferred_time_start, preferred_time_end, confirmed_time, helper_id) " +
            "VALUES(#{orderNo}, #{ownerId}, #{building}, #{unit}, #{room}, #{category}, #{description}, #{images}, " +
            "#{emergencyLevel}, #{status}, #{dispatcherId}, #{workerId}, #{assignedAt}, #{acceptedAt}, " +
            "#{completedAt}, #{repairDuration}, #{repairNote}, #{laborCost}, #{materialCost}, #{needRevisit}, #{timeoutAt}, #{isTimeout}, " +
            "#{urgeCount}, #{lastUrgeAt}, #{closeReason}, #{closedBy}, #{closedAt}, " +
            "#{preferredTimeStart}, #{preferredTimeEnd}, #{confirmedTime}, #{helperId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RepairOrder order);

    @Delete("DELETE FROM repair_order WHERE id = #{id}")
    int deleteById(Integer id);

    @Update("UPDATE repair_order SET status=#{status}, dispatcher_id=#{dispatcherId}, worker_id=#{workerId}, " +
            "assigned_at=#{assignedAt}, accepted_at=#{acceptedAt}, completed_at=#{completedAt}, " +
            "repair_duration=#{repairDuration}, repair_note=#{repairNote}, labor_cost=#{laborCost}, " +
            "material_cost=#{materialCost}, need_revisit=#{needRevisit}, is_timeout=#{isTimeout}, " +
            "timeout_at=#{timeoutAt}, urge_count=#{urgeCount}, last_urge_at=#{lastUrgeAt}, " +
            "close_reason=#{closeReason}, closed_by=#{closedBy}, closed_at=#{closedAt}, " +
            "preferred_time_start=#{preferredTimeStart}, preferred_time_end=#{preferredTimeEnd}, " +
            "confirmed_time=#{confirmedTime}, helper_id=#{helperId} WHERE id=#{id}")
    int updateById(RepairOrder order);

    @Select("SELECT * FROM repair_order WHERE id = #{id}")
    RepairOrder selectById(Integer id);

    @Select("SELECT * FROM repair_order WHERE owner_id = #{ownerId} ORDER BY created_at DESC")
    List<RepairOrder> selectByOwnerId(Integer ownerId);

    @Select("SELECT * FROM repair_order WHERE worker_id = #{workerId} OR helper_id = #{workerId} ORDER BY created_at DESC")
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
     * 统计维修工当前负荷：已派单(2)或维修中(3)的工单数（含协助工单）
     */
    @Select("SELECT COUNT(*) FROM repair_order WHERE (worker_id = #{workerId} OR helper_id = #{workerId}) AND status IN (2,3)")
    Integer countActiveByWorkerId(Integer workerId);

    // ===== 工单流程增强：定向更新 =====

    /**
     * 催单：次数+1，记录最近催单时间
     */
    @Update("UPDATE repair_order SET urge_count = urge_count + 1, last_urge_at = NOW() WHERE id = #{id}")
    int updateUrge(Integer id);

    /**
     * 关闭工单（业主取消/客服驳回/超时自动关闭）：6-已关闭 7-超时关闭
     */
    @Update("UPDATE repair_order SET status = #{status}, close_reason = #{reason}, closed_by = #{closedBy}, " +
            "closed_at = NOW() WHERE id = #{id}")
    int closeOrder(@Param("id") Integer id, @Param("status") Integer status,
                   @Param("reason") String reason, @Param("closedBy") Integer closedBy);

    /**
     * 转派：更换维修工，重置为待接单状态，重设超时时间
     */
    @Update("UPDATE repair_order SET worker_id = #{workerId}, status = 2, assigned_at = NOW(), " +
            "accepted_at = NULL, is_timeout = 0, timeout_at = #{timeoutAt} WHERE id = #{id}")
    int updateReassign(@Param("id") Integer id, @Param("workerId") Integer workerId,
                       @Param("timeoutAt") LocalDateTime timeoutAt);

    /**
     * 加派协助维修工
     */
    @Update("UPDATE repair_order SET helper_id = #{helperId} WHERE id = #{id}")
    int updateHelperId(@Param("id") Integer id, @Param("helperId") Integer helperId);

    /**
     * 维修工确认上门时间
     */
    @Update("UPDATE repair_order SET confirmed_time = #{confirmedTime} WHERE id = #{id}")
    int updateConfirmedTime(@Param("id") Integer id, @Param("confirmedTime") LocalDateTime confirmedTime);

    // ===== 超时扫描 =====

    /**
     * 查询已过超时时间但未标记的超时工单（待派单/待接单/维修中）
     */
    @Select("SELECT * FROM repair_order WHERE status IN (1,2,3) AND timeout_at IS NOT NULL " +
            "AND timeout_at < NOW() AND is_timeout = 0")
    List<RepairOrder> selectTimeoutOrders();

    /**
     * 标记超时
     */
    @Update("UPDATE repair_order SET is_timeout = 1 WHERE id = #{id}")
    int markTimeout(Integer id);

    /**
     * 查询待派单超时且超过宽限期的工单（自动关闭用）
     */
    @Select("SELECT * FROM repair_order WHERE status = 1 AND timeout_at IS NOT NULL " +
            "AND timeout_at < DATE_SUB(NOW(), INTERVAL #{graceHours} HOUR)")
    List<RepairOrder> selectOverduePendingOrders(@Param("graceHours") int graceHours);

    // ===== 维修工月度统计 =====

    /**
     * 统计维修工本月完工数（status 4已完工/5已评价，created_at 在本月）
     */
    @Select("SELECT COUNT(*) FROM repair_order WHERE worker_id = #{workerId} AND status IN (4,5) " +
            "AND created_at >= DATE_FORMAT(NOW(), '%Y-%m-01')")
    Long countCompletedThisMonthByWorkerId(Integer workerId);

    /**
     * 统计维修工本月已完成工单的总耗时
     */
    @Select("SELECT IFNULL(SUM(repair_duration), 0) FROM repair_order WHERE worker_id = #{workerId} AND status IN (4,5) " +
            "AND created_at >= DATE_FORMAT(NOW(), '%Y-%m-01')")
    Long sumDurationThisMonthByWorkerId(Integer workerId);

    // ===== 看板统计 =====

    @Select("SELECT COUNT(*) FROM repair_order")
    Long countAll();

    @Select("SELECT COUNT(*) FROM repair_order WHERE status = #{status}")
    Long countByStatus(Integer status);

    @Select("SELECT status, COUNT(*) as count FROM repair_order GROUP BY status")
    List<Map<String, Object>> countByStatusGroup();

    @Select("SELECT category, COUNT(*) as count FROM repair_order GROUP BY category")
    List<Map<String, Object>> countByCategory();

    @Select("SELECT building, COUNT(*) as count FROM repair_order GROUP BY building ORDER BY count DESC")
    List<Map<String, Object>> countByBuilding();

    // 已完工且完工超过指定小时数（评价提醒扫描用）
    @Select("SELECT * FROM repair_order WHERE status = 4 AND completed_at <= DATE_SUB(NOW(), INTERVAL #{hours} HOUR)")
    List<RepairOrder> selectCompletedBeforeHours(Integer hours);

    // ===== 基础数据删除保护：统计引用 =====

    @Select("SELECT COUNT(*) FROM repair_order WHERE category = #{category}")
    Long countOrdersByCategory(String category);

    @Select("SELECT COUNT(*) FROM repair_order WHERE building = #{building}")
    Long countOrdersByBuilding(String building);

    @Select("SELECT COUNT(*) FROM repair_order WHERE building = #{building} AND unit = #{unit}")
    Long countOrdersByBuildingAndUnit(@Param("building") String building, @Param("unit") String unit);


    @Select("SELECT DATE(created_at) as date, COUNT(*) as count " +
            "FROM repair_order " +
            "WHERE created_at >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date ASC")
    List<Map<String, Object>> selectTrend(@Param("days") int days);

    // ===== 维修工工作看板 =====

    /**
     * 统计维修工某状态工单数（2待接单/3维修中）
     */
    @Select("SELECT COUNT(*) FROM repair_order WHERE worker_id = #{workerId} AND status = #{status}")
    Long countByWorkerIdAndStatus(@Param("workerId") Integer workerId, @Param("status") Integer status);

    /**
     * 近7天维修工每日完工数（按 completed_at 分组）
     */
    @Select("SELECT DATE(completed_at) as date, COUNT(*) as count " +
            "FROM repair_order " +
            "WHERE worker_id = #{workerId} AND status IN (4,5) " +
            "AND completed_at >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(completed_at) " +
            "ORDER BY date ASC")
    List<Map<String, Object>> selectCompletedTrendByWorkerId(@Param("workerId") Integer workerId, @Param("days") int days);

    // ===== 客服工作量看板 =====

    /**
     * 统计客服今日已派单数（dispatcher_id + assigned_at 在今天）
     */
    @Select("SELECT COUNT(*) FROM repair_order WHERE dispatcher_id = #{dispatcherId} " +
            "AND assigned_at >= CURDATE() AND assigned_at < CURDATE() + INTERVAL 1 DAY")
    Long countTodayAssignedByDispatcherId(Integer dispatcherId);

    /**
     * 统计今日待派单数（status=1 且今天创建）
     */
    @Select("SELECT COUNT(*) FROM repair_order WHERE status = 1 " +
            "AND created_at >= CURDATE() AND created_at < CURDATE() + INTERVAL 1 DAY")
    Long countTodayPending();

    /**
     * 统计客服总派单数
     */
    @Select("SELECT COUNT(*) FROM repair_order WHERE dispatcher_id = #{dispatcherId}")
    Long countTotalAssignedByDispatcherId(Integer dispatcherId);

    /**
     * 近7天客服每日派单数（按 assigned_at 分组）
     */
    @Select("SELECT DATE(assigned_at) as date, COUNT(*) as count " +
            "FROM repair_order " +
            "WHERE dispatcher_id = #{dispatcherId} " +
            "AND assigned_at >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(assigned_at) " +
            "ORDER BY date ASC")
    List<Map<String, Object>> selectAssignTrendByDispatcherId(@Param("dispatcherId") Integer dispatcherId, @Param("days") int days);

    /**
     * 今日各紧急程度报修数量（按 emergency_level 分组）
     */
    @Select("SELECT emergency_level as level, COUNT(*) as count FROM repair_order " +
            "WHERE created_at >= CURDATE() AND created_at < CURDATE() + INTERVAL 1 DAY " +
            "GROUP BY emergency_level")
    List<Map<String, Object>> selectTodayEmergencyGroup();

    // ===== 业主端首页统计 =====

    @Select("SELECT COUNT(*) FROM repair_order WHERE owner_id = #{ownerId}")
    Long countByOwnerId(Integer ownerId);

    @Select("SELECT COUNT(*) FROM repair_order WHERE owner_id = #{ownerId} AND status IN (1,2,3)")
    Long countPendingByOwnerId(Integer ownerId);

    @Select("SELECT COUNT(*) FROM repair_order WHERE owner_id = #{ownerId} AND status IN (4,5)")
    Long countCompletedByOwnerId(Integer ownerId);

    @Select("SELECT DATE(created_at) as date, COUNT(*) as count " +
            "FROM repair_order " +
            "WHERE owner_id = #{ownerId} AND created_at >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY date ASC")
    List<Map<String, Object>> selectTrendByOwnerId(@Param("ownerId") Integer ownerId, @Param("days") int days);
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

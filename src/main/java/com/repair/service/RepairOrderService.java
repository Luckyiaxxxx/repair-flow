package com.repair.service;


import com.repair.dto.CompleteOrderRequest;
import com.repair.entity.Evaluation;
import com.repair.entity.RepairOrder;
import com.repair.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface RepairOrderService {

    RepairOrder submitOrder(RepairOrder order,Integer ownerId);

    List<RepairOrder> getOrdersByOwnerId(Integer ownerId);

    RepairOrder getOrderById(Integer orderId);

    void assignOrder(Integer orderId , Integer workerId , Integer dispatcherId);

    List<RepairOrder> getPendingOrders();

    List<RepairOrder> getOrdersByWorkerId(Integer workerId);

    void acceptOrder(Integer orderId,Integer workerId);

    void completeOrder(Integer orderId, CompleteOrderRequest request);

    void evaluateOrder(Evaluation evaluation);

    void deleteOrder(Integer orderId, Integer ownerId);

    Map<String, Object> searchOrders(Integer status, String category, String building, String startDate, String endDate, Integer page, Integer pageSize);

    /** 按楼栋统计报修分布 */
    List<Map<String, Object>> getBuildingStats();

    Map<String, Object> getOwnerStatistics(Integer ownerId);

    Map<String, Object> getWorkerMonthlyStatistics(Integer workerId);

    Map<String, Object> getWorkerDashboard(Integer workerId);

    Map<String, Object> getDispatcherDashboard(Integer dispatcherId);

    // ===== A组：工单流程增强 =====

    /** 业主取消报修（仅待派单） */
    void cancelOrder(Integer orderId, Integer ownerId);

    /** 业主催单（间隔限制+次数上限） */
    void urgeOrder(Integer orderId, Integer ownerId);

    /**
     * 转派/改派：更换维修工，重置为待接单
     * @param source 1-客服转派 2-转单申请同意
     */
    void reassignOrder(Integer orderId, Integer newWorkerId, Integer dispatcherId, Integer source);

    /** 加派协助维修工 */
    void addHelper(Integer orderId, Integer helperId, Integer dispatcherId);

    /** 客服驳回无效工单（仅待派单，需驳回原因） */
    void rejectOrder(Integer orderId, Integer dispatcherId, String reason);

    /** 维修工列表（派单/转派选择用，仅启用中） */
    List<User> getWorkers();

    /** 维修工确认上门时间 */
    void confirmAppointment(Integer orderId, Integer workerId, LocalDateTime confirmedTime);

}

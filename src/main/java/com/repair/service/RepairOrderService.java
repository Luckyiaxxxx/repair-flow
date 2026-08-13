package com.repair.service;


import com.repair.dto.CompleteOrderRequest;
import com.repair.entity.Evaluation;
import com.repair.entity.RepairOrder;

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

}

package com.repair.service;


import com.repair.entity.Evaluation;
import com.repair.entity.RepairOrder;

import java.util.List;

public interface RepairOrderService {

    RepairOrder submitOrder(RepairOrder order,Integer ownerId);

    List<RepairOrder> getOrdersByOwnerId(Integer ownerId);

    RepairOrder getOrderById(Integer orderId);

    void assignOrder(Integer orderId , Integer workerId , Integer dispatcherId);

    List<RepairOrder> getPendingOrders();

    List<RepairOrder> getOrdersByWorkerId(Integer workerId);

    void acceptOrder(Integer orderId,Integer workerId);

    void completeOrder(Integer orderId,String repairNote,Integer repairDuration,Double laborCost,Double materialCost);

    void evaluateOrder(Evaluation evaluation);

    void deleteOrder(Integer orderId, Integer ownerId);

}

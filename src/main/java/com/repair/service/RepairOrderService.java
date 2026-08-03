package com.repair.service;


import com.repair.entity.RepairOrder;

import java.util.List;

public interface RepairOrderService {

    /**
     * 提交报修
     * @param order 保修单信息
     * @param ownerId 业主id
     * @return 创建成功的报修单
     */
    RepairOrder submitOrder(RepairOrder order,Integer ownerId);

    /**
     * 根据业主ID查询报修列表
     * @param ownerId 业主ID
     * @return 报修单列表
     */
    List<RepairOrder> getOrdersByOwnerId(Integer ownerId);

    /**
     * 根据ID查询报修详情
     * @param orderId 报修单ID
     * @return 报修单详情
     */
    RepairOrder getOrderById(Integer orderId);

    /**
     * 客服派单
     * @param orderId  报修单ID
     * @param workerId  维修工ID
     * @param dispatcherId  客服ID
     */
    void assignOrder(Integer orderId , Integer workerId , Integer dispatcherId);

    /**
     *
     * @return “待处理”的代派单
     */
    List<RepairOrder> getPendingOrders();

    /**
     * 查询维修工的工单列表
     * @param workerId   维修工ID
     * @return  维修工列表
     */
    List<RepairOrder> getOrdersByWorkerId(Integer workerId);

    /**
     * 维修工接单
     * @param orderId 报修单Id
     * @param workerId 维修工Id
     */
    void acceptOrder(Integer orderId,Integer workerId);

    /**
     *
     * @param orderId 报修单id
     * @param repairNote   维修备注
     * @param repairDuration  维修时长
     */
    void completeOrder(Integer orderId,String repairNote,Integer repairDuration);




}

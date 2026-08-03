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
}

package com.repair.service;

import com.repair.entity.OrderMessage;

import java.util.List;

public interface OrderMessageService {

    /**
     * 发送留言
     * @param senderRole 1-业主 2-客服 3-维修工
     */
    OrderMessage sendMessage(Integer orderId, Integer senderId, Integer senderRole, String content);

    /**
     * 查看工单留言（需校验参与人身份）
     */
    List<OrderMessage> getMessages(Integer orderId, Integer senderId, Integer senderRole);
}
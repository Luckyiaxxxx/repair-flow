package com.repair.service.impl;

import com.repair.common.BusinessException;
import com.repair.entity.OrderMessage;
import com.repair.entity.RepairOrder;
import com.repair.mapper.OrderMessageMapper;
import com.repair.mapper.RepairOrderMapper;
import com.repair.service.OrderMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderMessageServiceImpl implements OrderMessageService {

    @Autowired
    private OrderMessageMapper orderMessageMapper;

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    @Override
    public OrderMessage sendMessage(Integer orderId, Integer senderId, Integer senderRole, String content){
        RepairOrder order = checkParticipant(orderId, senderId, senderRole);

        //已关闭(6)/超时关闭(7)的工单不可留言
        if(order.getStatus() == 6 || order.getStatus() == 7){
            throw new BusinessException("该工单已关闭，无法留言");
        }
        if(content == null || content.trim().isEmpty()){
            throw new BusinessException("留言内容不能为空");
        }
        if(content.trim().length() > 500){
            throw new BusinessException("留言内容不能超过500字");
        }

        OrderMessage message = new OrderMessage();
        message.setOrderId(orderId);
        message.setSenderId(senderId);
        message.setSenderRole(senderRole);
        message.setContent(content.trim());
        int row = orderMessageMapper.insert(message);
        if(row <= 0){
            throw new BusinessException("留言失败，请稍后重试");
        }
        System.out.println("工单留言: orderId=" + orderId + ", senderId=" + senderId + ", role=" + senderRole + ", content=" + content);
        return message;
    }

    @Override
    public List<OrderMessage> getMessages(Integer orderId, Integer senderId, Integer senderRole){
        checkParticipant(orderId, senderId, senderRole);
        List<OrderMessage> messages = orderMessageMapper.selectByOrderId(orderId);
        return messages == null ? List.of() : messages;
    }

    /**
     * 校验留言参与人身份：业主=报修人，维修工=派单维修工或协助人，客服=任意
     */
    private RepairOrder checkParticipant(Integer orderId, Integer senderId, Integer senderRole){
        RepairOrder order = repairOrderMapper.selectById(orderId);
        if(order == null){
            throw new BusinessException("报修单不存在");
        }
        if(senderRole == null){
            throw new BusinessException("发送人角色不能为空");
        }
        switch (senderRole){
            case 1: // 业主：必须是报修人
                if(!order.getOwnerId().equals(senderId)){
                    throw new BusinessException("无权查看此工单留言");
                }
                break;
            case 3: // 维修工：必须是派单维修工或协助人
                boolean isWorker = (order.getWorkerId() != null && order.getWorkerId().equals(senderId))
                        || (order.getHelperId() != null && order.getHelperId().equals(senderId));
                if(!isWorker){
                    throw new BusinessException("无权查看此工单留言");
                }
                break;
            case 2: // 客服：允许
                break;
            default:
                throw new BusinessException("无效的发送人角色");
        }
        return order;
    }
}
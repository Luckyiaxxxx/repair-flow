package com.repair.service.impl;

import com.repair.common.BusinessException;
import com.repair.entity.OrderTransfer;
import com.repair.entity.RepairOrder;
import com.repair.entity.User;
import com.repair.mapper.OrderTransferMapper;
import com.repair.mapper.RepairOrderMapper;
import com.repair.mapper.UserMapper;
import com.repair.service.OrderTransferService;
import com.repair.service.RepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderTransferServiceImpl implements OrderTransferService {

    @Autowired
    private OrderTransferMapper orderTransferMapper;

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    @Autowired
    private RepairOrderService repairOrderService;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public OrderTransfer requestTransfer(Integer orderId, Integer workerId, Integer type, String reason){
        RepairOrder order = repairOrderMapper.selectById(orderId);
        if(order == null){
            throw new BusinessException("报修单不存在");
        }
        //仅维修中(3)可发起
        if(order.getStatus() != 3){
            throw new BusinessException("仅维修中的工单可以申请转单/协助");
        }
        //必须是当前维修工本人（协助人不可发起）
        if(order.getWorkerId() == null || !order.getWorkerId().equals(workerId)){
            throw new BusinessException("只有当前维修工可以发起申请");
        }
        if(type == null || (type != 1 && type != 2)){
            throw new BusinessException("申请类型无效：1-转单 2-申请协助");
        }
        if(reason == null || reason.trim().isEmpty()){
            throw new BusinessException("申请原因不能为空");
        }
        //同一工单不可重复申请
        List<OrderTransfer> pendingList = orderTransferMapper.selectPendingByOrderId(orderId);
        if(pendingList != null && !pendingList.isEmpty()){
            throw new BusinessException("该工单已有待处理的申请");
        }

        OrderTransfer transfer = new OrderTransfer();
        transfer.setOrderId(orderId);
        transfer.setFromWorkerId(workerId);
        transfer.setType(type);
        transfer.setReason(reason.trim());
        transfer.setStatus(0);
        int row = orderTransferMapper.insert(transfer);
        if(row <= 0){
            throw new BusinessException("申请失败，请稍后重试");
        }
        System.out.println("维修工发起申请: orderId=" + orderId + ", workerId=" + workerId + ", type=" + type + ", reason=" + reason);
        return transfer;
    }

    @Override
    public List<OrderTransfer> getPendingTransfers(){
        List<OrderTransfer> list = orderTransferMapper.selectPendingAll();
        return list == null ? new ArrayList<>() : list;
    }

    @Override
    public List<OrderTransfer> getMyTransfers(Integer workerId){
        if(workerId == null){
            throw new BusinessException("维修工ID不能为空");
        }
        List<OrderTransfer> list = orderTransferMapper.selectByWorkerId(workerId);
        return list == null ? new ArrayList<>() : list;
    }

    @Override
    @Transactional
    public void approveTransfer(Integer transferId, Integer dispatcherId, Integer newWorkerId){
        OrderTransfer transfer = orderTransferMapper.selectById(transferId);
        if(transfer == null){
            throw new BusinessException("申请不存在");
        }
        if(transfer.getStatus() != 0){
            throw new BusinessException("该申请已处理，请勿重复操作");
        }
        if(newWorkerId == null){
            throw new BusinessException("请指定维修工");
        }
        //校验新维修工
        User newWorker = userMapper.selectById(newWorkerId);
        if(newWorker == null){
            throw new BusinessException("维修工不存在");
        }
        if(newWorker.getRole() != 3){
            throw new BusinessException("该用户不是维修工");
        }
        if(transfer.getFromWorkerId().equals(newWorkerId)){
            throw new BusinessException("不能指定发起人本人");
        }

        if(transfer.getType() == 1){
            //转单：更换维修工（source=2 转单同意）
            repairOrderService.reassignOrder(transfer.getOrderId(), newWorkerId, dispatcherId, 2);
        }else{
            //协助：加派协助维修工
            repairOrderService.addHelper(transfer.getOrderId(), newWorkerId, dispatcherId);
        }

        orderTransferMapper.updateHandle(transferId, 1, dispatcherId, newWorkerId, null);
        System.out.println("客服同意申请: transferId=" + transferId + ", newWorkerId=" + newWorkerId);
    }

    @Override
    @Transactional
    public void rejectTransfer(Integer transferId, Integer dispatcherId, String note){
        OrderTransfer transfer = orderTransferMapper.selectById(transferId);
        if(transfer == null){
            throw new BusinessException("申请不存在");
        }
        if(transfer.getStatus() != 0){
            throw new BusinessException("该申请已处理，请勿重复操作");
        }
        if(note == null || note.trim().isEmpty()){
            throw new BusinessException("拒绝原因不能为空");
        }
        orderTransferMapper.updateHandle(transferId, 2, dispatcherId, null, note.trim());
        System.out.println("客服拒绝申请: transferId=" + transferId + ", note=" + note);
    }
}
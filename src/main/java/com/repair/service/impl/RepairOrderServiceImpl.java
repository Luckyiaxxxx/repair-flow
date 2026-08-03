package com.repair.service.impl;

import com.repair.common.BusinessException;
import com.repair.entity.RepairOrder;
import com.repair.entity.User;
import com.repair.mapper.RepairOrderMapper;
import com.repair.mapper.UserMapper;
import com.repair.service.RepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @description:
 * @author: 徐家豪
 * @date: 2026/8/1 16:13
 * @version: 1.0
 */
@Service
public class RepairOrderServiceImpl implements RepairOrderService {

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public RepairOrder submitOrder(RepairOrder order ,Integer ownerId){
        //1.校验业主是否存在
        User owner = userMapper.selectById(ownerId);
        if(owner == null){
            throw new BusinessException("业主不存在");
        }
        //2.校验报修描述不能为空
        if(order.getDescription()==null||order.getDescription().trim().isEmpty()){
            throw new BusinessException("报修类别不能为空");
        }
        //3.校验报修类别不能为空
        if(order.getCategory()==null||order.getCategory().trim().isEmpty()){
            throw new BusinessException("报修类别不能为空");
        }
        //4.设置业主信息
        order.setOwnerId(ownerId);
        order.setBuilding(order.getBuilding()!=null?order.getBuilding():owner.getBuilding());
        order.setUnit(order.getUnit()!=null?order.getUnit():owner.getUnit());
        order.setRoom(order.getRoom() != null ? order.getRoom() : owner.getRoom());

        //5.生成工单编号：RU+时间戳 +4位随机数
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);

        //6.设置默认状态：代派单
        order.setStatus(1);

        //7.设置紧急程度：普通1
        if(order.getEmergencyLevel()==null){
            order.setEmergencyLevel(1);
        }

        //8.保存到数据库
        int row = repairOrderMapper.insert(order);
        if(row<=0){
            throw  new BusinessException("提交报修失败，请稍后重试");
        }
        return order;
    }

    @Override
    public List<RepairOrder> getOrdersByOwnerId(Integer ownerId){
        if(ownerId==null){
            throw new BusinessException("业主ID不能为空");
        }
        return  repairOrderMapper.selectByOwnerId(ownerId);
    }

    @Override
    public RepairOrder getOrderById(Integer orderId){
        if(orderId==null){
            throw new BusinessException("报修单ID不能为空");
        }
        RepairOrder order = repairOrderMapper.selectById(orderId);
        if(order==null){
            throw new BusinessException("报修单不存在");
        }
        return order;
    }

    @Override
    public List<RepairOrder> getPendingOrders(){
        return repairOrderMapper.selectPendingOrders();
    }

    @Override
    @Transactional
    public void assignOrder(Integer orderId,Integer workerId,Integer dispatcherId){
        //1.校验报修单是否存在
        RepairOrder order = repairOrderMapper.selectById(orderId);
        if(order == null){
            throw new BusinessException("该报单不存在");
        }

        //2.校验保修单状态是否位“代派单”
        if(order.getStatus()!=1){
            throw new BusinessException("该报修单已被处理，无法派单");
        }

        //3.校验维修工是否存在且角色是维修工
        User worker = userMapper.selectById(workerId);
        if(worker==null){
            throw new BusinessException("维修工不存在");
        }
        if(worker.getRole()!=3){
            throw new BusinessException("该用户不是维修工");
        }

        //4.校验客服是否存在
        User dispatcher = userMapper.selectById(dispatcherId);
        if(dispatcher ==null){
            throw new BusinessException("客服不存在");
        }

        //5.更新报修单
        order.setWorkerId(workerId);
        order.setDispatcherId(dispatcherId);
        order.setStatus(2);
        order.setAssignedAt(LocalDateTime.now());

        //6.保存到数据库
        repairOrderMapper.updateById(order);
    }


    @Override
    public List<RepairOrder> getOrdersByWorkerId(Integer workerId){
        if(workerId==null){
            throw new BusinessException("维修工Id不能为空");
        }
        return repairOrderMapper.selectByWorkerId(workerId);
    }

    @Override
    @Transactional
    public void acceptOrder(Integer orderId, Integer workerId){
        //1.校验报修单是否存在
        RepairOrder order = repairOrderMapper.selectById(orderId);
        if(order==null){
            throw new BusinessException("报修单不存在");
        }

        //2.校验报修单状态是否为“已派单”
        if(order.getStatus()!=2){
            throw new BusinessException("该报修单无法接单");
        }

        //3.校验维修工是否存在且角色是维修工
        User worker = userMapper.selectById(workerId);
        if(worker == null){
            throw new BusinessException("维修工不存在");
        }
        if(worker.getRole()!=3){
            throw new BusinessException("该用户不是维修工");
        }

        //4.更新报修单
        order.setWorkerId(workerId);
        order.setStatus(3);
        order.setAcceptedAt(LocalDateTime.now());

        //5.保存
        repairOrderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void completeOrder(Integer orderId,String repairNote,Integer repairDuration){
        //1.校验报修单是否存在
        RepairOrder order =repairOrderMapper.selectById(orderId);
        if(order == null){
            throw new BusinessException("保修单不存在");
        }

        //2.校验报修单状态是否为“维修中”
        if(order.getStatus()!=3){
            throw new BusinessException("该保修单无法完工");
        }

        //3.校验维修记录不能为空
        if(repairNote==null || repairNote.trim().isEmpty()){
            throw new BusinessException("维修记录不能为空");
        }

        //4.校验维修耗时不能为空
        if(repairDuration ==null ||repairDuration<=0){
            throw new BusinessException("维修耗时必须大于0");
        }

        //5.更新报修单
        order.setStatus(4);
        order.setRepairNote(repairNote);
        order.setRepairDuration(repairDuration);
        order.setCompletedAt(LocalDateTime.now());

        //6.保存
        repairOrderMapper.updateById(order);
    }



    /**
     *生成工单编号：RU+yyyyMMddHHmmss+4位随机数
     */
    private String generateOrderNo(){
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d",(int)(Math.random()*10000));
        return "RU"+timestamp+random;
    }
}

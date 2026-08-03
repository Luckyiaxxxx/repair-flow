package com.repair.service.impl;

import com.repair.common.BusinessException;
import com.repair.entity.RepairOrder;
import com.repair.entity.User;
import com.repair.mapper.RepairOrderMapper;
import com.repair.mapper.UserMapper;
import com.repair.service.RepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


    /**
     *生成工单编号：RU+yyyyMMddHHmmss+4位随机数
     */
    private String generateOrderNo(){
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d",(int)(Math.random()*10000));
        return "RU"+timestamp+random;
    }
}

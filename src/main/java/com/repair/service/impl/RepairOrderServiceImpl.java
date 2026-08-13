package com.repair.service.impl;

import com.repair.common.BusinessException;
import com.repair.entity.Evaluation;
import com.repair.entity.RepairOrder;
import com.repair.entity.User;
import com.repair.mapper.EvaluationMapper;
import com.repair.mapper.RepairOrderMapper;
import com.repair.mapper.UserMapper;
import com.repair.service.RepairOrderService;
import com.repair.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private RedisUtil redisUtil;

    private static final String CACHE_KEY_ORDER = "repair:order:";
    private static final String CACHE_KEY_ORDERS = "repair:orders:owner:";
    private static final long CACHE_EXPIRE_TIME = 30; // 30分钟

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

        //清楚该业主的列表缓存
        String cacheKey = CACHE_KEY_ORDERS + ownerId;
        redisUtil.delete(cacheKey);
        System.out.println("清除缓存"+cacheKey);
        return order;
    }

    @Override
    public List<RepairOrder> getOrdersByOwnerId(Integer ownerId){
        if(ownerId==null){
            throw new BusinessException("业主ID不能为空");
        }

        //1.先从缓存取
        String cacheKey = CACHE_KEY_ORDERS+ownerId;
        Object cached = redisUtil.get(cacheKey);
        if(cached!=null){
            System.out.println("从缓存获取报修列表:ownerId="+ownerId);
            return (List<RepairOrder>) cached;
        }

        //2.缓存没有，查数据库
        System.out.println("从数据库查询报修列表: ownerId=" + ownerId);
        List<RepairOrder> orders = repairOrderMapper.selectByOwnerId(ownerId);


        //3.存数据库
        if(orders!=null&&!orders.isEmpty()){
            redisUtil.set(cacheKey,orders,CACHE_EXPIRE_TIME,TimeUnit.MINUTES);
            System.out.println("报修列表已缓存: ownerId=" + ownerId);
        }
        return orders;
    }

    @Override
    public RepairOrder getOrderById(Integer orderId){
        if(orderId==null){
            throw new BusinessException("报修单ID不能为空");
        }

        //1.先从缓存里取
        String cacheKey = CACHE_KEY_ORDER + orderId;
        Object cached = redisUtil.get(cacheKey);
        if(cached!=null){
            System.out.println("从缓存获取报修单"+orderId);
            // 处理类型转换：如果存的是 LinkedHashMap，转成 RepairOrder
            if (cached instanceof RepairOrder) {
                return (RepairOrder) cached;
            } else {
                // 用 Jackson 转成 RepairOrder
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                return mapper.convertValue(cached, RepairOrder.class);
            }
        }

        //2.缓存没有，查数据库
        System.out.println("从数据库里查询报修单："+ orderId);
        RepairOrder order = repairOrderMapper.selectById(orderId);
        if(order==null){
            throw new BusinessException("报修单不存在");
        }

        //3.存入缓存
        redisUtil.set(cacheKey, order, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        System.out.println("报修单已缓存"+orderId);
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
    public void completeOrder(Integer orderId,String repairNote,Integer repairDuration,Double laborCost,Double materialCost){
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
        order.setLaborCost(laborCost);
        order.setMaterialCost(materialCost);
        order.setCompletedAt(LocalDateTime.now());

        //6.保存
        repairOrderMapper.updateById(order);

        //7.清除缓存
        String orderCacheKey = CACHE_KEY_ORDER +orderId;
        String listCacheKey = CACHE_KEY_ORDERS + order.getOwnerId();
        redisUtil.delete(orderCacheKey);
        redisUtil.delete(listCacheKey);
        System.out.println("清除缓存"+ orderCacheKey + ", " + listCacheKey);
    }

    @Override
    @Transactional
    public void evaluateOrder(Evaluation evaluation){
        //1.校验报修单是否存在
        RepairOrder order = repairOrderMapper.selectById(evaluation.getOrderId());
        if(order ==null){
            throw new BusinessException("报修单不存在");
        }

        //2.校验是否为业主本人
        if(!order.getOwnerId().equals(evaluation.getOwnerId())){
            throw new BusinessException("无权评价此报修单");
        }

        //3.校验维修状态是否为已完工
        if(order.getStatus()!=4){
            throw new BusinessException("该报修单还没有完工，无法评价");
        }

        //4.校验是否已经评价过
        Evaluation existing = evaluationMapper.selectByOrderId(evaluation.getOrderId());
        if(existing!=null){
            throw new BusinessException("已评价，不可重复评价");
        }

        //5.设置维修工ID
        evaluation.setWorkerId(order.getWorkerId());

        //6.保存评价
        evaluationMapper.insert(evaluation);

        //7.更新报修单状态
        order.setStatus(5);
        repairOrderMapper.updateById(order);

        //8.清除缓存
        String orderCacheKey = CACHE_KEY_ORDER + evaluation.getOrderId();
        String listCacheKey = CACHE_KEY_ORDERS + evaluation.getOwnerId();
        redisUtil.delete(orderCacheKey);
        redisUtil.delete(listCacheKey);
    }

    @Override
    public void deleteOrder(Integer orderId, Integer ownerId) {
        System.out.println("deleteOrder called: orderId=" + orderId + ", ownerId=" + ownerId);
        RepairOrder order = repairOrderMapper.selectById(orderId);
        System.out.println("selectById result: " + order);
        if (order == null) {
            throw new BusinessException("报修单不存在, orderId=" + orderId);
        }
        if (!order.getOwnerId().equals(ownerId)) {
            throw new BusinessException("无权删除此报修单");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("仅待派单状态的报修单可以删除");
        }
        repairOrderMapper.deleteById(orderId);
        redisUtil.delete(CACHE_KEY_ORDER + orderId);
        redisUtil.delete(CACHE_KEY_ORDERS + ownerId);
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

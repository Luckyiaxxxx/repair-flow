package com.repair.service.impl;

import com.repair.common.BusinessException;
import com.repair.dto.CompleteOrderRequest;
import com.repair.entity.Evaluation;
import com.repair.entity.Material;
import com.repair.entity.MaterialConsumption;
import com.repair.entity.OrderReassignLog;
import com.repair.entity.RepairOrder;
import com.repair.entity.SysMessage;
import com.repair.entity.User;
import com.repair.mapper.EvaluationMapper;
import com.repair.mapper.MaterialConsumptionMapper;
import com.repair.mapper.MaterialMapper;
import com.repair.mapper.OrderReassignLogMapper;
import com.repair.mapper.RepairOrderMapper;
import com.repair.mapper.UserMapper;
import com.repair.service.MessageService;
import com.repair.service.RepairOrderService;
import com.repair.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private MaterialMapper materialMapper;

    @Autowired
    private MaterialConsumptionMapper materialConsumptionMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OrderReassignLogMapper orderReassignLogMapper;

    @Autowired
    private MessageService messageService;

    // ===== 工单流程配置（application.yml 可调） =====
    @Value("${repair.order.dispatch-timeout-hours:24}")
    private int dispatchTimeoutHours; // 待派单超时（小时）

    @Value("${repair.order.accept-timeout-hours:12}")
    private int acceptTimeoutHours; // 待接单超时（小时）

    @Value("${repair.order.complete-timeout-hours:48}")
    private int completeTimeoutHours; // 维修超时（小时）

    @Value("${repair.order.urge-min-interval-minutes:30}")
    private int urgeMinIntervalMinutes; // 催单最小间隔（分钟）

    @Value("${repair.order.urge-max-count:5}")
    private int urgeMaxCount; // 每单最大催单次数

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

        //5.生成工单编号：RU+日期+4位自增序号
        String orderNo = generateOrderNo();
        order.setOrderNo(orderNo);

        //6.设置默认状态：代派单
        order.setStatus(1);

        //7.设置紧急程度：普通1
        if(order.getEmergencyLevel()==null){
            order.setEmergencyLevel(1);
        }

        //8.校验预约上门时间段：要么都为空，要么开始<结束
        if(order.getPreferredTimeStart()!=null || order.getPreferredTimeEnd()!=null){
            if(order.getPreferredTimeStart()==null || order.getPreferredTimeEnd()==null){
                throw new BusinessException("预约上门时间需同时填写开始和结束时间");
            }
            if(!order.getPreferredTimeEnd().isAfter(order.getPreferredTimeStart())){
                throw new BusinessException("预约上门结束时间必须晚于开始时间");
            }
        }

        //9.设置默认值：催单次数、超时标记、待派单超时时间
        order.setUrgeCount(0);
        order.setIsTimeout(0);
        order.setTimeoutAt(LocalDateTime.now().plusHours(dispatchTimeoutHours));

        //10.保存到数据库
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

        //2.校验保修单状态是否位"代派单"
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

        //4.校验维修工当前负荷：已派单(2)、维修中(3)的工单数达到max_workload则拒绝派单
        if(worker.getMaxWorkload() != null && worker.getMaxWorkload() > 0){
            int currentLoad = repairOrderMapper.countActiveByWorkerId(workerId);
            if(currentLoad >= worker.getMaxWorkload()){
                throw new BusinessException("该维修工已满负荷，请选择其他维修工");
            }
        }

        //5.校验客服是否存在
        User dispatcher = userMapper.selectById(dispatcherId);
        if(dispatcher ==null){
            throw new BusinessException("客服不存在");
        }

        //6.更新报修单：重设待接单超时时间
        order.setWorkerId(workerId);
        order.setDispatcherId(dispatcherId);
        order.setStatus(2);
        order.setAssignedAt(LocalDateTime.now());
        order.setTimeoutAt(LocalDateTime.now().plusHours(acceptTimeoutHours));

        //7.保存到数据库
        repairOrderMapper.updateById(order);

        //8.站内消息：派单通知维修工
        messageService.sendMessage(workerId, SysMessage.TYPE_ASSIGN, "新维修任务",
                "您有新的维修任务：" + order.getOrderNo() + "（" + order.getBuilding() + order.getUnit() + order.getRoom() + "），请及时接单", orderId);
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

        //2.校验报修单状态是否为"已派单"
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

        //4.更新报修单：重设维修超时时间
        order.setWorkerId(workerId);
        order.setStatus(3);
        order.setAcceptedAt(LocalDateTime.now());
        order.setTimeoutAt(LocalDateTime.now().plusHours(completeTimeoutHours));

        //5.保存
        repairOrderMapper.updateById(order);

        //6.站内消息：接单通知业主
        messageService.sendMessage(order.getOwnerId(), SysMessage.TYPE_ACCEPT, "报修已被接单",
                "您的报修单 " + order.getOrderNo() + " 已被维修工接单，维修工将尽快上门处理", orderId);

        //7.清除缓存
        String orderCacheKey = CACHE_KEY_ORDER + orderId;
        String listCacheKey = CACHE_KEY_ORDERS + order.getOwnerId();
        redisUtil.delete(orderCacheKey);
        redisUtil.delete(listCacheKey);
    }

    @Override
    @Transactional
    public void completeOrder(Integer orderId, CompleteOrderRequest request){
        //1.校验报修单是否存在
        RepairOrder order =repairOrderMapper.selectById(orderId);
        if(order == null){
            throw new BusinessException("保修单不存在");
        }

        //2.校验报修单状态是否为"维修中"
        if(order.getStatus()!=3){
            throw new BusinessException("该保修单无法完工");
        }

        //3.校验维修记录不能为空
        if(request.getRepairNote()==null || request.getRepairNote().trim().isEmpty()){
            throw new BusinessException("维修记录不能为空");
        }

        //4.校验维修耗时不能为空
        if(request.getRepairDuration() ==null ||request.getRepairDuration()<=0){
            throw new BusinessException("维修耗时必须大于0");
        }

        //5.处理物料消耗：自动扣库存 + 计算物料总费用
        double totalMaterialCost = 0.0;
        if(request.getMaterials() != null && !request.getMaterials().isEmpty()){
            for(CompleteOrderRequest.MaterialItem item : request.getMaterials()){
                if(item.getMaterialId() == null){
                    throw new BusinessException("物料ID不能为空");
                }
                if(item.getQuantity() == null || item.getQuantity() <= 0){
                    throw new BusinessException("物料消耗数量必须大于0");
                }
                Material material = materialMapper.selectById(item.getMaterialId());
                if(material == null){
                    throw new BusinessException("物料不存在：ID=" + item.getMaterialId());
                }
                if(material.getStock() < item.getQuantity()){
                    throw new BusinessException("物料[" + material.getName() + "]库存不足，当前库存：" + material.getStock() + "，需要：" + item.getQuantity());
                }
                int rows = materialMapper.deductStock(item.getMaterialId(), item.getQuantity());
                if(rows <= 0){
                    throw new BusinessException("物料[" + material.getName() + "]扣减库存失败");
                }
                MaterialConsumption consumption = new MaterialConsumption();
                consumption.setOrderId(orderId);
                consumption.setMaterialId(item.getMaterialId());
                consumption.setQuantity(item.getQuantity());
                materialConsumptionMapper.insert(consumption);
                if(material.getPrice() != null){
                    totalMaterialCost += material.getPrice() * item.getQuantity();
                }
            }
        }

        //6.更新报修单
        order.setStatus(4);
        order.setRepairNote(request.getRepairNote());
        order.setRepairDuration(request.getRepairDuration());
        order.setLaborCost(request.getLaborCost());
        order.setMaterialCost(totalMaterialCost);
        order.setCompletedAt(LocalDateTime.now());

        //7.保存
        repairOrderMapper.updateById(order);

        //8.清除缓存
        String orderCacheKey = CACHE_KEY_ORDER +orderId;
        String listCacheKey = CACHE_KEY_ORDERS + order.getOwnerId();
        redisUtil.delete(orderCacheKey);
        redisUtil.delete(listCacheKey);
        System.out.println("清除缓存"+ orderCacheKey + ", " + listCacheKey);

        //9.站内消息：完工通知业主（请及时评价）
        messageService.sendMessage(order.getOwnerId(), SysMessage.TYPE_COMPLETE, "报修已完工",
                "您的报修单 " + order.getOrderNo() + " 已完工，请及时对本次服务进行评价", orderId);
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
     *生成工单编号：RU+yyyyMMdd+4位自增序号（每日从0001开始）
     */
    private String generateOrderNo(){
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "RU" + date;
        String maxOrderNo = repairOrderMapper.selectMaxOrderNoByPrefix(prefix);
        int seq = 1;
        if(maxOrderNo != null && !maxOrderNo.isEmpty()){
            seq = Integer.parseInt(maxOrderNo.substring(maxOrderNo.length() - 4)) + 1;
        }
        return prefix + String.format("%04d", seq);
    }

    @Override
    public Map<String, Object> searchOrders(Integer status, String category, String building, String startDate, String endDate, Integer page, Integer pageSize){
        if(page == null || page < 1) page = 1;
        if(pageSize == null || pageSize < 1) pageSize = 10;
        int offset = (page - 1) * pageSize;

        Long total = repairOrderMapper.countSearch(status, category, building, startDate, endDate);
        List<RepairOrder> list = repairOrderMapper.search(status, category, building, startDate, endDate, offset, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", list);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public List<Map<String, Object>> getBuildingStats() {
        return repairOrderMapper.countByBuilding();
    }

    @Override
    public Map<String, Object> getOwnerStatistics(Integer ownerId){
        //1.校验业主是否存在
        if(ownerId==null){
            throw new BusinessException("业主ID不能为空");
        }
        User owner = userMapper.selectById(ownerId);
        if(owner==null){
            throw new BusinessException("业主不存在");
        }

        Map<String, Object> data = new LinkedHashMap<>();

        //2.核心统计：总报修数、待处理数(1,2,3)、已完成数(4,5)
        Long total = repairOrderMapper.countByOwnerId(ownerId);
        data.put("total", total != null ? total : 0L);

        Long pending = repairOrderMapper.countPendingByOwnerId(ownerId);
        data.put("pending", pending != null ? pending : 0L);

        Long completed = repairOrderMapper.countCompletedByOwnerId(ownerId);
        data.put("completed", completed != null ? completed : 0L);

        //3.近7天趋势（含今天）
        List<Map<String, Object>> trendData = repairOrderMapper.selectTrendByOwnerId(ownerId, 6);
        Map<String, Long> trendMap = new HashMap<>();
        for (Map<String, Object> row : trendData) {
            String date = row.get("date").toString().substring(5); // yyyy-MM-dd -> MM-dd
            trendMap.put(date, ((Number) row.get("count")).longValue());
        }

        List<String> trend = new ArrayList<>();
        List<Integer> trendCounts = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).format(formatter);
            trend.add(date);
            trendCounts.add(trendMap.getOrDefault(date, 0L).intValue());
        }
        data.put("trend", trend);
        data.put("trendCounts", trendCounts);

        System.out.println("业主统计结果: ownerId=" + ownerId + ", data=" + data);
        return data;
    }

    @Override
    public Map<String, Object> getWorkerMonthlyStatistics(Integer workerId){
        if(workerId==null){
            throw new BusinessException("维修工ID不能为空");
        }
        User worker = userMapper.selectById(workerId);
        if(worker==null){
            throw new BusinessException("维修工不存在");
        }

        Map<String, Object> data = new LinkedHashMap<>();

        //本月完工数
        Long completedCount = repairOrderMapper.countCompletedThisMonthByWorkerId(workerId);
        data.put("completedCount", completedCount != null ? completedCount : 0L);

        //本月已完成工单总耗时
        Long totalDuration = repairOrderMapper.sumDurationThisMonthByWorkerId(workerId);
        data.put("totalDuration", totalDuration != null ? totalDuration : 0L);

        //所有评价的平均分
        Double avgScore = evaluationMapper.selectAvgScoreByWorkerId(workerId);
        data.put("avgScore", avgScore != null ? avgScore : 0.0);

        return data;
    }

    @Override
    public Map<String, Object> getWorkerDashboard(Integer workerId){
        if(workerId==null){
            throw new BusinessException("维修工ID不能为空");
        }
        User worker = userMapper.selectById(workerId);
        if(worker==null){
            throw new BusinessException("维修工不存在");
        }

        Map<String, Object> data = new LinkedHashMap<>();

        //本月完工数、总耗时
        Long monthlyCompleted = repairOrderMapper.countCompletedThisMonthByWorkerId(workerId);
        data.put("monthlyCompleted", monthlyCompleted != null ? monthlyCompleted : 0L);

        Long totalDuration = repairOrderMapper.sumDurationThisMonthByWorkerId(workerId);
        data.put("totalDuration", totalDuration != null ? totalDuration : 0L);

        //平均评分（保留1位小数）
        Double avgScore = evaluationMapper.selectAvgScoreByWorkerId(workerId);
        double roundedScore = avgScore == null ? 0.0 : Math.round(avgScore * 10) / 10.0;
        data.put("avgScore", roundedScore);

        //当前待接单数(已派单)与维修中数
        Long pendingAccept = repairOrderMapper.countByWorkerIdAndStatus(workerId, 2);
        data.put("pendingAccept", pendingAccept != null ? pendingAccept : 0L);

        Long inProgress = repairOrderMapper.countByWorkerIdAndStatus(workerId, 3);
        data.put("inProgress", inProgress != null ? inProgress : 0L);

        //近7天完工趋势（含今天，按完工时间）
        List<Map<String, Object>> trendData = repairOrderMapper.selectCompletedTrendByWorkerId(workerId, 6);
        Map<String, Long> trendMap = new HashMap<>();
        for (Map<String, Object> row : trendData) {
            String date = row.get("date").toString().substring(5); // yyyy-MM-dd -> MM-dd
            trendMap.put(date, ((Number) row.get("count")).longValue());
        }

        List<String> trendDates = new ArrayList<>();
        List<Integer> trendCounts = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).format(formatter);
            trendDates.add(date);
            trendCounts.add(trendMap.getOrDefault(date, 0L).intValue());
        }
        data.put("trendDates", trendDates);
        data.put("trendCounts", trendCounts);

        return data;
    }

    @Override
    public Map<String, Object> getDispatcherDashboard(Integer dispatcherId){
        if(dispatcherId==null){
            throw new BusinessException("客服ID不能为空");
        }
        User dispatcher = userMapper.selectById(dispatcherId);
        if(dispatcher==null){
            throw new BusinessException("客服不存在");
        }

        Map<String, Object> data = new LinkedHashMap<>();

        //今日已派单数、今日待派单数、总派单数
        Long todayAssigned = repairOrderMapper.countTodayAssignedByDispatcherId(dispatcherId);
        data.put("todayAssigned", todayAssigned != null ? todayAssigned : 0L);

        Long todayPending = repairOrderMapper.countTodayPending();
        data.put("todayPending", todayPending != null ? todayPending : 0L);

        Long totalAssigned = repairOrderMapper.countTotalAssignedByDispatcherId(dispatcherId);
        data.put("totalAssigned", totalAssigned != null ? totalAssigned : 0L);

        //近7天派单趋势（含今天，按派单时间）
        List<Map<String, Object>> trendData = repairOrderMapper.selectAssignTrendByDispatcherId(dispatcherId, 6);
        Map<String, Long> trendMap = new HashMap<>();
        for (Map<String, Object> row : trendData) {
            String date = row.get("date").toString().substring(5); // yyyy-MM-dd -> MM-dd
            trendMap.put(date, ((Number) row.get("count")).longValue());
        }

        List<String> trendDates = new ArrayList<>();
        List<Integer> trendCounts = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).format(formatter);
            trendDates.add(date);
            trendCounts.add(trendMap.getOrDefault(date, 0L).intValue());
        }
        data.put("trendDates", trendDates);
        data.put("trendCounts", trendCounts);

        //今日各紧急程度报修数量（1普通/2紧急/3特急）
        List<Map<String, Object>> emergencyData = repairOrderMapper.selectTodayEmergencyGroup();
        Map<String, Long> emMap = new LinkedHashMap<>();
        emMap.put("普通", 0L);
        emMap.put("紧急", 0L);
        emMap.put("特急", 0L);
        for (Map<String, Object> row : emergencyData) {
            int level = ((Number) row.get("level")).intValue();
            String name = level == 2 ? "紧急" : (level == 3 ? "特急" : "普通");
            emMap.put(name, ((Number) row.get("count")).longValue());
        }
        data.put("emergencyDistribution", emMap);

        return data;
    }

    // ==================== A组：工单流程增强 ====================

    /**
     * 业主取消报修（仅待派单状态可取消）
     */
    @Override
    @Transactional
    public void cancelOrder(Integer orderId, Integer ownerId){
        RepairOrder order = repairOrderMapper.selectById(orderId);
        if(order == null){
            throw new BusinessException("报修单不存在");
        }
        if(!order.getOwnerId().equals(ownerId)){
            throw new BusinessException("无权取消此报修单");
        }
        if(order.getStatus() != 1){
            throw new BusinessException("该报修单已被处理，无法取消");
        }
        //状态6=已关闭
        repairOrderMapper.closeOrder(orderId, 6, "业主取消报修", ownerId);
        clearOrderCache(orderId, order.getOwnerId());
        System.out.println("业主取消报修: orderId=" + orderId + ", ownerId=" + ownerId);
    }

    /**
     * 业主催单：30分钟内不可重复催单，每单最多5次
     */
    @Override
    @Transactional
    public void urgeOrder(Integer orderId, Integer ownerId){
        RepairOrder order = repairOrderMapper.selectById(orderId);
        if(order == null){
            throw new BusinessException("报修单不存在");
        }
        if(!order.getOwnerId().equals(ownerId)){
            throw new BusinessException("无权催单");
        }
        if(order.getStatus() != 1 && order.getStatus() != 2 && order.getStatus() != 3){
            throw new BusinessException("该报修单当前状态无法催单");
        }
        //间隔限制
        if(order.getLastUrgeAt() != null){
            long minutes = Duration.between(order.getLastUrgeAt(), LocalDateTime.now()).toMinutes();
            if(minutes < urgeMinIntervalMinutes){
                throw new BusinessException("请勿频繁催单，距上次催单不足" + urgeMinIntervalMinutes + "分钟");
            }
        }
        //次数上限
        int urgeCount = order.getUrgeCount() == null ? 0 : order.getUrgeCount();
        if(urgeCount >= urgeMaxCount){
            throw new BusinessException("催单次数已达上限（" + urgeMaxCount + "次）");
        }
        repairOrderMapper.updateUrge(orderId);
        clearOrderCache(orderId, order.getOwnerId());
        System.out.println("业主催单: orderId=" + orderId + ", 第" + (urgeCount + 1) + "次");
    }

    /**
     * 转派/改派：更换维修工，工单重置为待接单(2)
     * @param source 1-客服转派 2-转单申请同意
     */
    @Override
    @Transactional
    public void reassignOrder(Integer orderId, Integer newWorkerId, Integer dispatcherId, Integer source){
        RepairOrder order = repairOrderMapper.selectById(orderId);
        if(order == null){
            throw new BusinessException("报修单不存在");
        }
        //仅已派单(2)/维修中(3)可转派
        if(order.getStatus() != 2 && order.getStatus() != 3){
            throw new BusinessException("该报修单当前状态无法转派");
        }
        //新维修工校验
        User newWorker = userMapper.selectById(newWorkerId);
        if(newWorker == null){
            throw new BusinessException("维修工不存在");
        }
        if(newWorker.getRole() != 3){
            throw new BusinessException("该用户不是维修工");
        }
        if(order.getWorkerId() != null && order.getWorkerId().equals(newWorkerId)){
            throw new BusinessException("不能转派给当前维修工");
        }
        //负荷校验
        if(newWorker.getMaxWorkload() != null && newWorker.getMaxWorkload() > 0){
            int currentLoad = repairOrderMapper.countActiveByWorkerId(newWorkerId);
            if(currentLoad >= newWorker.getMaxWorkload()){
                throw new BusinessException("该维修工已满负荷，请选择其他维修工");
            }
        }
        //客服校验
        User dispatcher = userMapper.selectById(dispatcherId);
        if(dispatcher == null || dispatcher.getRole() != 2){
            throw new BusinessException("客服不存在");
        }

        Integer oldWorkerId = order.getWorkerId();

        //更新工单：换维修工、重置待接单、重设超时
        repairOrderMapper.updateReassign(orderId, newWorkerId,
                LocalDateTime.now().plusHours(acceptTimeoutHours));

        //记录转派日志
        OrderReassignLog log = new OrderReassignLog();
        log.setOrderId(orderId);
        log.setOldWorkerId(oldWorkerId);
        log.setNewWorkerId(newWorkerId);
        log.setDispatcherId(dispatcherId);
        log.setSource(source == null ? 1 : source);
        orderReassignLogMapper.insert(log);

        //站内消息：转派通知新维修工
        messageService.sendMessage(newWorkerId, SysMessage.TYPE_ASSIGN, "转派维修任务",
                "您有新的维修任务（转派）：" + order.getOrderNo() + "（" + order.getBuilding() + order.getUnit() + order.getRoom() + "），请及时接单", orderId);

        clearOrderCache(orderId, order.getOwnerId());
        System.out.println("工单转派: orderId=" + orderId + ", " + oldWorkerId + " -> " + newWorkerId + ", source=" + source);
    }

    /**
     * 加派协助维修工（申请协助同意后调用）
     */
    @Override
    @Transactional
    public void addHelper(Integer orderId, Integer helperId, Integer dispatcherId){
        RepairOrder order = repairOrderMapper.selectById(orderId);
        if(order == null){
            throw new BusinessException("报修单不存在");
        }
        if(order.getStatus() != 2 && order.getStatus() != 3){
            throw new BusinessException("该报修单当前状态无法加派协助");
        }
        User helper = userMapper.selectById(helperId);
        if(helper == null){
            throw new BusinessException("维修工不存在");
        }
        if(helper.getRole() != 3){
            throw new BusinessException("该用户不是维修工");
        }
        if(order.getWorkerId() != null && order.getWorkerId().equals(helperId)){
            throw new BusinessException("协助人不能是当前维修工本人");
        }
        if(order.getHelperId() != null){
            throw new BusinessException("该工单已有协助维修工");
        }
        //负荷校验
        if(helper.getMaxWorkload() != null && helper.getMaxWorkload() > 0){
            int currentLoad = repairOrderMapper.countActiveByWorkerId(helperId);
            if(currentLoad >= helper.getMaxWorkload()){
                throw new BusinessException("该维修工已满负荷，请选择其他维修工");
            }
        }
        User dispatcher = userMapper.selectById(dispatcherId);
        if(dispatcher == null || dispatcher.getRole() != 2){
            throw new BusinessException("客服不存在");
        }

        repairOrderMapper.updateHelperId(orderId, helperId);

        //记录转派日志：来源3-协助加派
        OrderReassignLog log = new OrderReassignLog();
        log.setOrderId(orderId);
        log.setOldWorkerId(null);
        log.setNewWorkerId(helperId);
        log.setDispatcherId(dispatcherId);
        log.setSource(3);
        orderReassignLogMapper.insert(log);

        //站内消息：通知协助维修工
        messageService.sendMessage(helperId, SysMessage.TYPE_ASSIGN, "协助维修任务",
                "您被加派为协助维修工：" + order.getOrderNo() + "（" + order.getBuilding() + order.getUnit() + order.getRoom() + "），请及时联系主维修工", orderId);

        clearOrderCache(orderId, order.getOwnerId());
        System.out.println("协助加派: orderId=" + orderId + ", helperId=" + helperId);
    }

    /**
     * 客服驳回无效工单（仅待派单，需驳回原因）
     */
    @Override
    @Transactional
    public void rejectOrder(Integer orderId, Integer dispatcherId, String reason){
        RepairOrder order = repairOrderMapper.selectById(orderId);
        if(order == null){
            throw new BusinessException("报修单不存在");
        }
        if(order.getStatus() != 1){
            throw new BusinessException("仅待派单状态的报修单可以驳回");
        }
        if(reason == null || reason.trim().isEmpty()){
            throw new BusinessException("驳回原因不能为空");
        }
        User dispatcher = userMapper.selectById(dispatcherId);
        if(dispatcher == null || dispatcher.getRole() != 2){
            throw new BusinessException("客服不存在");
        }
        //状态6=已关闭
        repairOrderMapper.closeOrder(orderId, 6, reason.trim(), dispatcherId);
        clearOrderCache(orderId, order.getOwnerId());
        System.out.println("客服驳回工单: orderId=" + orderId + ", reason=" + reason);
    }

    /**
     * 维修工列表（启用中，role=3）
     */
    @Override
    public List<User> getWorkers(){
        List<User> workers = userMapper.selectByRole(3);
        if(workers == null){
            return new ArrayList<>();
        }
        //过滤禁用账号：status=0为禁用，null视为正常
        workers.removeIf(u -> u.getStatus() != null && u.getStatus() == 0);
        return workers;
    }

    /**
     * 维修工确认上门时间（仅维修中，本人或协助人）
     */
    @Override
    @Transactional
    public void confirmAppointment(Integer orderId, Integer workerId, LocalDateTime confirmedTime){
        RepairOrder order = repairOrderMapper.selectById(orderId);
        if(order == null){
            throw new BusinessException("报修单不存在");
        }
        if(order.getStatus() != 3){
            throw new BusinessException("仅维修中的工单可以确认上门时间");
        }
        boolean isWorker = (order.getWorkerId() != null && order.getWorkerId().equals(workerId))
                || (order.getHelperId() != null && order.getHelperId().equals(workerId));
        if(!isWorker){
            throw new BusinessException("无权确认此工单的上门时间");
        }
        if(confirmedTime == null){
            throw new BusinessException("上门时间不能为空");
        }
        repairOrderMapper.updateConfirmedTime(orderId, confirmedTime);
        clearOrderCache(orderId, order.getOwnerId());
        System.out.println("确认上门时间: orderId=" + orderId + ", time=" + confirmedTime);
    }

    /**
     * 清除工单相关缓存
     */
    private void clearOrderCache(Integer orderId, Integer ownerId){
        String orderCacheKey = CACHE_KEY_ORDER + orderId;
        String listCacheKey = CACHE_KEY_ORDERS + ownerId;
        redisUtil.delete(orderCacheKey);
        redisUtil.delete(listCacheKey);
        System.out.println("清除缓存 " + orderCacheKey + ", " + listCacheKey);
    }
}

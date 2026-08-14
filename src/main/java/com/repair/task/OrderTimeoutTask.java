package com.repair.task;

import com.repair.entity.RepairOrder;
import com.repair.mapper.RepairOrderMapper;
import com.repair.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 工单超时扫描定时任务
 * 1. 待派单/待接单/维修中超过 timeout_at 未推进 -> 标记 is_timeout=1
 * 2. 待派单超时且超过宽限期 -> 自动关闭（7=超时关闭）
 */
@Component
public class OrderTimeoutTask {

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Value("${repair.order.auto-close-grace-hours:24}")
    private int autoCloseGraceHours; // 待派单超时后的宽限期（小时），超过后自动关闭

    /**
     * 每5分钟扫描一次（间隔可通过 repair.order.timeout-scan-ms 调整）
     */
    @Scheduled(fixedDelayString = "${repair.order.timeout-scan-ms:300000}")
    public void scanTimeoutOrders(){
        System.out.println("[超时扫描] 开始扫描超时工单...");

        //1.标记超时工单
        List<RepairOrder> timeoutOrders = repairOrderMapper.selectTimeoutOrders();
        if(timeoutOrders != null){
            for(RepairOrder order : timeoutOrders){
                repairOrderMapper.markTimeout(order.getId());
                redisUtil.delete("repair:order:" + order.getId());
                System.out.println("[超时扫描] 工单已超时: orderId=" + order.getId() + ", orderNo=" + order.getOrderNo() + ", status=" + order.getStatus());
            }
        }

        //2.待派单超时且超过宽限期 -> 自动关闭（7=超时关闭）
        List<RepairOrder> overduePending = repairOrderMapper.selectOverduePendingOrders(autoCloseGraceHours);
        if(overduePending != null){
            for(RepairOrder order : overduePending){
                repairOrderMapper.closeOrder(order.getId(), 7, "待派单超时自动关闭", null);
                redisUtil.delete("repair:order:" + order.getId());
                redisUtil.delete("repair:orders:owner:" + order.getOwnerId());
                System.out.println("[超时扫描] 待派单超时自动关闭: orderId=" + order.getId() + ", orderNo=" + order.getOrderNo());
            }
        }

        System.out.println("[超时扫描] 扫描完成：超时标记" + (timeoutOrders == null ? 0 : timeoutOrders.size())
                + "单，自动关闭" + (overduePending == null ? 0 : overduePending.size()) + "单");
    }
}
package com.repair.task;

import com.repair.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 评价提醒定时任务：扫描已完工超时未评价的工单，向业主发送站内消息提醒
 */
@Component
public class EvaluateRemindTask {

    @Autowired
    private MessageService messageService;

    @Scheduled(fixedDelayString = "${repair.order.timeout-scan-ms:300000}")
    public void scanAndRemind() {
        try {
            int sent = messageService.sendEvaluateReminders(null);
            if (sent > 0) {
                System.out.println("[评价提醒] 已发送 " + sent + " 条评价提醒");
            }
        } catch (Exception e) {
            System.out.println("[评价提醒] 扫描异常: " + e.getMessage());
        }
    }
}
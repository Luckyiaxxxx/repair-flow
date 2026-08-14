package com.repair.service;

import com.repair.entity.SysMessage;

import java.util.Map;

public interface MessageService {

    /** 发送站内消息 */
    SysMessage sendMessage(Integer receiverId, Integer type, String title, String content, Integer orderId);

    /** 分页查询我的消息（含未读总数） */
    Map<String, Object> listMessages(Integer userId, Integer isRead, Integer page, Integer pageSize);

    /** 消息详情（仅接收人可见） */
    SysMessage getMessage(Integer id, Integer userId);

    /** 标记单条已读 */
    void markRead(Integer id, Integer userId);

    /** 全部标记已读，返回更新条数 */
    int markAllRead(Integer userId);

    /** 删除消息（仅接收人可删） */
    void deleteMessage(Integer id, Integer userId);

    /** 未读消息数 */
    long unreadCount(Integer userId);

    /**
     * 扫描已完工超时未评价的工单，向业主发送评价提醒（每单只发一次）
     * @param minHours 完工距今小时数阈值，null 用配置默认值
     * @return 本次发送条数
     */
    int sendEvaluateReminders(Integer minHours);
}
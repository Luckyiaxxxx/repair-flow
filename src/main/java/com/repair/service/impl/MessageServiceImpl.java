package com.repair.service.impl;

import com.repair.common.BusinessException;
import com.repair.entity.RepairOrder;
import com.repair.entity.SysMessage;
import com.repair.mapper.RepairOrderMapper;
import com.repair.mapper.SysMessageMapper;
import com.repair.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private SysMessageMapper sysMessageMapper;

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    @Value("${repair.order.evaluate-remind-hours:24}")
    private int evaluateRemindHours;

    @Override
    @Transactional
    public SysMessage sendMessage(Integer receiverId, Integer type, String title, String content, Integer orderId) {
        if (receiverId == null) {
            throw new BusinessException("接收人ID不能为空");
        }
        if (type == null || type < 1 || type > 5) {
            throw new BusinessException("消息类型不合法");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException("消息标题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("消息内容不能为空");
        }
        SysMessage message = new SysMessage();
        message.setReceiverId(receiverId);
        message.setType(type);
        message.setTitle(title.trim());
        message.setContent(content.trim());
        message.setOrderId(orderId);
        message.setIsRead(0);
        sysMessageMapper.insert(message);
        return message;
    }

    @Override
    public Map<String, Object> listMessages(Integer userId, Integer isRead, Integer page, Integer pageSize) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (isRead != null && isRead != 0 && isRead != 1) {
            throw new BusinessException("已读状态值只能为0或1");
        }
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        int offset = (page - 1) * pageSize;

        Long total = sysMessageMapper.countPage(userId, isRead);
        Long unread = sysMessageMapper.countUnread(userId);
        List<SysMessage> list = sysMessageMapper.selectPage(userId, isRead, offset, pageSize);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("unreadCount", unread);
        result.put("list", list);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public SysMessage getMessage(Integer id, Integer userId) {
        SysMessage message = getOwnedMessage(id, userId);
        return message;
    }

    @Override
    @Transactional
    public void markRead(Integer id, Integer userId) {
        SysMessage message = getOwnedMessage(id, userId);
        if (message.getIsRead() != null && message.getIsRead() == 1) {
            return; // 已读，幂等
        }
        sysMessageMapper.updateRead(id, userId);
    }

    @Override
    @Transactional
    public int markAllRead(Integer userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        return sysMessageMapper.markAllRead(userId);
    }

    @Override
    @Transactional
    public void deleteMessage(Integer id, Integer userId) {
        getOwnedMessage(id, userId);
        sysMessageMapper.deleteById(id);
    }

    @Override
    public long unreadCount(Integer userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        Long count = sysMessageMapper.countUnread(userId);
        return count != null ? count : 0L;
    }

    @Override
    @Transactional
    public int sendEvaluateReminders(Integer minHours) {
        int hours = minHours != null ? minHours : evaluateRemindHours;
        if (hours < 0) {
            throw new BusinessException("小时数不能为负数");
        }
        List<RepairOrder> orders = repairOrderMapper.selectCompletedBeforeHours(hours);
        int sent = 0;
        for (RepairOrder order : orders) {
            if (order.getOwnerId() == null) {
                continue;
            }
            Long existed = sysMessageMapper.countByOrderIdAndType(order.getId(), SysMessage.TYPE_EVALUATE_REMIND, order.getOwnerId());
            if (existed != null && existed > 0) {
                continue;
            }
            sendMessage(order.getOwnerId(), SysMessage.TYPE_EVALUATE_REMIND, "评价提醒",
                    "您的报修单 " + order.getOrderNo() + " 已完工，请您及时对本次维修服务进行评价", order.getId());
            sent++;
        }
        return sent;
    }

    private SysMessage getOwnedMessage(Integer id, Integer userId) {
        if (id == null) {
            throw new BusinessException("消息ID不能为空");
        }
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        SysMessage message = sysMessageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        if (!message.getReceiverId().equals(userId)) {
            throw new BusinessException("无权操作该消息");
        }
        return message;
    }
}
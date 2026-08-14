package com.repair.controller.common;

//站内消息中心（各角色共用）

import com.repair.common.Result;
import com.repair.entity.SysMessage;
import com.repair.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping
    public Result<Map<String, Object>> listMessages(
            @RequestParam Integer userId,
            @RequestParam(required = false) Integer isRead,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> data = messageService.listMessages(userId, isRead, page, pageSize);
        return Result.success(data);
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount(@RequestParam Integer userId) {
        long count = messageService.unreadCount(userId);
        return Result.success(count);
    }

    @GetMapping("/{id}")
    public Result<SysMessage> getMessage(@PathVariable Integer id, @RequestParam Integer userId) {
        SysMessage message = messageService.getMessage(id, userId);
        return Result.success(message);
    }

    @PutMapping("/{id}/read")
    public Result<String> markRead(@PathVariable Integer id, @RequestParam Integer userId) {
        messageService.markRead(id, userId);
        return Result.success("已标记为已读");
    }

    @PutMapping("/read-all")
    public Result<String> markAllRead(@RequestParam Integer userId) {
        int rows = messageService.markAllRead(userId);
        return Result.success("已读 " + rows + " 条消息");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteMessage(@PathVariable Integer id, @RequestParam Integer userId) {
        messageService.deleteMessage(id, userId);
        return Result.success("删除成功");
    }
}
package com.repair.service;

import com.repair.entity.Announcement;

import java.util.List;
import java.util.Map;

public interface AnnouncementService {

    Announcement addAnnouncement(Announcement announcement);

    void deleteAnnouncement(Integer id);

    Announcement updateAnnouncement(Announcement announcement);

    Announcement getAnnouncementById(Integer id);

    List<Announcement> listAllAnnouncements();

    List<Announcement> listPublishedAnnouncements();

    // ==================== 公告已读/未读 ====================

    /** 标记公告已读（幂等） */
    void markRead(Integer announcementId, Integer userId);

    /** 未读公告数（启用中公告总数 - 已读数） */
    long getUnreadCount(Integer userId);

    /** 启用中公告列表（带 isRead 字段） */
    List<Map<String, Object>> listPublishedWithReadStatus(Integer userId);
}
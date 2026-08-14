package com.repair.service.impl;

import com.repair.common.BusinessException;
import com.repair.entity.Announcement;
import com.repair.mapper.AnnouncementMapper;
import com.repair.mapper.AnnouncementReadMapper;
import com.repair.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Autowired
    private AnnouncementReadMapper announcementReadMapper;

    @Override
    @Transactional
    public Announcement addAnnouncement(Announcement announcement) {
        if (announcement.getTitle() == null || announcement.getTitle().trim().isEmpty()) {
            throw new BusinessException("公告标题不能为空");



        }
        if (announcement.getContent() == null || announcement.getContent().trim().isEmpty()) {
            throw new BusinessException("公告内容不能为空");
        }
        if (announcement.getIsTop() == null) {
            announcement.setIsTop(0);
        }
        if (announcement.getStatus() == null) {
            announcement.setStatus(1);
        }
        announcementMapper.insert(announcement);
        return announcement;
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Integer id) {
        if (id == null) {
            throw new BusinessException("公告ID不能为空");
        }
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        announcementMapper.deleteById(id);
    }

    @Override
    @Transactional
    public Announcement updateAnnouncement(Announcement announcement) {
        if (announcement.getId() == null) {
            throw new BusinessException("公告ID不能为空");
        }
        Announcement existing = announcementMapper.selectById(announcement.getId());
        if (existing == null) {
            throw new BusinessException("公告不存在");
        }
        announcementMapper.updateById(announcement);
        return announcement;
    }

    @Override
    public Announcement getAnnouncementById(Integer id) {
        if (id == null) {
            throw new BusinessException("公告ID不能为空");
        }
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        return announcement;
    }

    @Override
    public List<Announcement> listAllAnnouncements() {
        return announcementMapper.selectAll();
    }

    @Override
    public List<Announcement> listPublishedAnnouncements() {
        return announcementMapper.selectPublished();
    }

    // ==================== 公告已读/未读 ====================

    @Override
    @Transactional
    public void markRead(Integer announcementId, Integer userId) {
        if (announcementId == null) {
            throw new BusinessException("公告ID不能为空");
        }
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        Announcement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        // 幂等：重复标记不报错
        announcementReadMapper.insert(announcementId, userId);
    }

    @Override
    public long getUnreadCount(Integer userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        Long published = announcementMapper.countPublished();
        Long read = announcementReadMapper.countReadEnabledByUserId(userId);
        long total = published != null ? published : 0L;
        long readCount = read != null ? read : 0L;
        return Math.max(total - readCount, 0L);
    }

    @Override
    public List<Map<String, Object>> listPublishedWithReadStatus(Integer userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        List<Announcement> announcements = announcementMapper.selectPublished();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Announcement announcement : announcements) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", announcement.getId());
            item.put("title", announcement.getTitle());
            item.put("content", announcement.getContent());
            item.put("type", announcement.getType());
            item.put("isTop", announcement.getIsTop());
            item.put("status", announcement.getStatus());
            item.put("createdAt", announcement.getCreatedAt());
            item.put("updatedAt", announcement.getUpdatedAt());
            item.put("isRead", announcementReadMapper.exists(announcement.getId(), userId) > 0);
            result.add(item);
        }
        return result;
    }
}
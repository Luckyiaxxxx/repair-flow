package com.repair.service.impl;

import com.repair.common.BusinessException;
import com.repair.entity.Announcement;
import com.repair.mapper.AnnouncementMapper;
import com.repair.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

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
}
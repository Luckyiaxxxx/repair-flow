package com.repair.service;

import com.repair.entity.Announcement;

import java.util.List;

public interface AnnouncementService {

    Announcement addAnnouncement(Announcement announcement);

    void deleteAnnouncement(Integer id);

    Announcement updateAnnouncement(Announcement announcement);

    Announcement getAnnouncementById(Integer id);

    List<Announcement> listAllAnnouncements();

    List<Announcement> listPublishedAnnouncements();
}
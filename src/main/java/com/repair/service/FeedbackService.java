package com.repair.service;

import com.repair.entity.Feedback;

import java.util.List;

public interface FeedbackService {

    Feedback submitFeedback(Feedback feedback);

    List<Feedback> getFeedbacksByOwnerId(Integer ownerId);

    Feedback getFeedbackById(Integer id);

    List<Feedback> listAllFeedbacks();

    void replyFeedback(Integer id, String reply);

    void deleteFeedback(Integer id);
}
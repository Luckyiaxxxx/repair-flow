package com.repair.service.impl;

import com.repair.common.BusinessException;
import com.repair.entity.Feedback;
import com.repair.mapper.FeedbackMapper;
import com.repair.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Override
    @Transactional
    public Feedback submitFeedback(Feedback feedback) {
        if (feedback.getOwnerId() == null) {
            throw new BusinessException("业主ID不能为空");
        }
        if (feedback.getTitle() == null || feedback.getTitle().trim().isEmpty()) {
            throw new BusinessException("标题不能为空");
        }
        if (feedback.getContent() == null || feedback.getContent().trim().isEmpty()) {
            throw new BusinessException("内容不能为空");
        }
        feedbackMapper.insert(feedback);
        return feedback;
    }

    @Override
    public List<Feedback> getFeedbacksByOwnerId(Integer ownerId) {
        if (ownerId == null) {
            throw new BusinessException("业主ID不能为空");
        }
        return feedbackMapper.selectByOwnerId(ownerId);
    }

    @Override
    public Feedback getFeedbackById(Integer id) {
        if (id == null) {
            throw new BusinessException("ID不能为空");
        }
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException("记录不存在");
        }
        return feedback;
    }

    @Override
    public List<Feedback> listAllFeedbacks() {
        return feedbackMapper.selectAll();
    }

    @Override
    @Transactional
    public void replyFeedback(Integer id, String reply) {
        if (id == null) {
            throw new BusinessException("ID不能为空");
        }
        if (reply == null || reply.trim().isEmpty()) {
            throw new BusinessException("回复内容不能为空");
        }
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException("记录不存在");
        }
        feedbackMapper.reply(id, reply);
    }

    @Override
    @Transactional
    public void deleteFeedback(Integer id) {
        if (id == null) {
            throw new BusinessException("ID不能为空");
        }
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new BusinessException("记录不存在");
        }
        feedbackMapper.deleteById(id);
    }
}
package com.repair.service.impl;

import com.repair.common.BusinessException;
import com.repair.entity.Evaluation;
import com.repair.entity.RepairOrder;
import com.repair.mapper.EvaluationMapper;
import com.repair.mapper.RepairOrderMapper;
import com.repair.service.EvaluationService;
import com.repair.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @description:
 * @author: 徐家豪
 * @date: 2026/8/10 16:16
 * @version: 1.0
 */

@Service
public class EvaluationServiceImpl implements EvaluationService {
    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    @Transactional
    public void addEvaluation(Evaluation evaluation) {
        // 1. 校验报修单是否存在
        RepairOrder order = repairOrderMapper.selectById(evaluation.getOrderId());
        if (order == null) {
            throw new BusinessException("报修单不存在");
        }

        // 2. 校验是否是业主本人
        if (!order.getOwnerId().equals(evaluation.getOwnerId())) {
            throw new BusinessException("无权评价此报修单");
        }

        // 3. 校验报修单状态必须是"已完工"（status=4）
        if (order.getStatus() != 4) {
            throw new BusinessException("该报修单还未完工，无法评价");
        }

        // 4. 校验是否已经评价过
        Evaluation existing = evaluationMapper.selectByOrderId(evaluation.getOrderId());
        if (existing != null) {
            throw new BusinessException("已评价，不可重复评价");
        }

        // 5. 校验评分
        if (evaluation.getScore() == null || evaluation.getScore() < 1 || evaluation.getScore() > 5) {
            throw new BusinessException("评分必须在1-5星之间");
        }

        // 6. 设置维修工ID
        evaluation.setWorkerId(order.getWorkerId());

        // 7. 保存评价
        evaluationMapper.insert(evaluation);

        // 8. 更新报修单状态为"已评价"（status=5）
        order.setStatus(5);
        repairOrderMapper.updateById(order);

        // 9. 清除缓存
        String orderCacheKey = "repair:order:" + evaluation.getOrderId();
        String listCacheKey = "repair:orders:owner:" + evaluation.getOwnerId();
        redisUtil.delete(orderCacheKey);
        redisUtil.delete(listCacheKey);
    }

    @Override
    public Evaluation getEvaluationById(Integer id) {
        if (id == null) {
            throw new BusinessException("评价ID不能为空");
        }
        Evaluation evaluation = evaluationMapper.selectById(id);
        if (evaluation == null) {
            throw new BusinessException("评价不存在");
        }
        return evaluation;
    }

    @Override
    public Evaluation getEvaluationByOrderId(Integer orderId) {
        if (orderId == null) {
            throw new BusinessException("报修单ID不能为空");
        }
        return evaluationMapper.selectByOrderId(orderId);
    }

    @Override
    public List<Evaluation> getEvaluationsByWorkerId(Integer workerId) {
        if (workerId == null) {
            throw new BusinessException("维修工ID不能为空");
        }
        return evaluationMapper.selectByWorkerId(workerId);
    }

    @Override
    public Double getAvgScoreByWorkerId(Integer workerId) {
        if (workerId == null) {
            throw new BusinessException("维修工ID不能为空");
        }
        return evaluationMapper.selectAvgScoreByWorkerId(workerId);
    }

    @Override
    public Integer getCountByWorkerId(Integer workerId) {
        if (workerId == null) {
            throw new BusinessException("维修工ID不能为空");
        }
        return evaluationMapper.selectCountByWorkerId(workerId);
    }
}
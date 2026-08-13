package com.repair.service;

import com.repair.entity.Evaluation;
import java.util.List;

public interface EvaluationService {

    /**
     * 新增评价
     */
    void addEvaluation(Evaluation evaluation);

    /**
     * 根据ID查询评价
     */
    Evaluation getEvaluationById(Integer id);

    /**
     * 根据报修单ID查询评价
     */
    Evaluation getEvaluationByOrderId(Integer orderId);

    /**
     * 查询维修工的所有评价
     */
    List<Evaluation> getEvaluationsByWorkerId(Integer workerId);

    /**
     * 查询维修工平均分
     */
    Double getAvgScoreByWorkerId(Integer workerId);

    /**
     * 查询维修工评价总数
     */
    Integer getCountByWorkerId(Integer workerId);
}
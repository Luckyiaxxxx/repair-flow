package com.repair.mapper;

import com.repair.entity.Evaluation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EvaluationMapper {
    //新增评价
    @Insert("INSERT INTO evaluation(order_id, owner_id, worker_id, score, comment) " +
            "VALUES(#{orderId}, #{ownerId}, #{workerId}, #{score}, #{comment})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Evaluation evaluation);

    //根据 ID 查询评价
    @Select("SELECT * FROM evaluation WHERE id = #{id}")
    Evaluation selectById(Integer id);

    //根据报修单 ID 查询评价
    @Select("SELECT * FROM evaluation WHERE order_id = #{orderId}")
    Evaluation selectByOrderId(Integer orderId);


    //根据维修工 ID 查询所有评价
    @Select("SELECT * FROM evaluation WHERE worker_id = #{workerId}")
    List<Evaluation> selectByWorkerId(Integer workerId);

    //查询维修工平均分
    @Select("SELECT AVG(score) FROM evaluation WHERE worker_id = #{workerId}")
    Double selectAvgScoreByWorkerId(Integer workerId);

    //查询维修工评价总数
    @Select("SELECT COUNT(*) FROM evaluation WHERE worker_id = #{workerId}")
    Integer selectCountByWorkerId(Integer workerId);
}

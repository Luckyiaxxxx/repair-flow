package com.repair.service.impl;

import com.repair.common.BusinessException;
import com.repair.entity.User;
import com.repair.mapper.EvaluationMapper;
import com.repair.mapper.UserMapper;
import com.repair.mapper.RepairOrderMapper;
import com.repair.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Override
    public User register(User user){

        //1.用户名称不为空
        if(user.getUsername()==null||user.getUsername().trim().isEmpty()){
            throw new BusinessException("用户名不能为空哟！");
        }

        //2.密码不为空且长度>=6
        if(user.getPassword()==null||user.getPassword().length()<6){
            throw new BusinessException("密码长度不能小于6哟！");
        }

        //3.手机号不为空
        if(user.getPhone() == null ||user.getPhone().trim().isEmpty()){
            throw new BusinessException("手机号不能为空哟！");
        }

        //4.检查用户名是否已存在
        User existing = userMapper.seletcByUsername(user.getUsername());
        if(existing !=null){
            throw new BusinessException("用户名已存在");
        }

        //5.设置默认值
        user.setStatus(1);

        //6.密码加密

        //7.保存到数据库
        int rows = userMapper.insert(user);
        if(rows <=0){
            throw new BusinessException("注册失败，请重试");
        }

        return user;
    }

    @Override
    public User login(String username,String password){

        //1.校验参数
        if(username == null ||username.trim().isEmpty()){
            throw new BusinessException("用户名不能为空");
        }
        if(password ==null||password.trim().isEmpty()){
            throw new BusinessException("密码不能为空");
        }

        //2.根据用户名查询用户
        User user = userMapper.seletcByUsername(username);
        if(user==null){
            throw new BusinessException("用户不存在");
        }

        //3.密码校验
        if(!password.equals(user.getPassword())){
            throw new BusinessException("密码错误");
        }

        //4.检查帐号状态
        if(user.getStatus()==0){
            throw new BusinessException("账号已禁用，请联系管理员了解明细");
        }
        return user;
    }

    @Override
    public User getUserById(Integer id) {
        if (id == null) {
            throw new BusinessException("用户ID不能为空");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    @Override
    public User getUserByUsername(String username){
        if(username==null||username.trim().isEmpty()){
            throw new BusinessException("用户名不能为空");
        }
        return userMapper.seletcByUsername(username);
    }

//    @Override
//    public List<Map<String,Object>> getworkerPerformanceList() {
//        //1.查询所有维修工
//        List<User> workers = userMapper.selectByRole(3);
//        if(workers ==null||workers.isEmpty()){
//            return new ArrayList<>();
//        }
//        List<Map<String,Object>> result = new ArrayList<>();
//
//        for(User worker : workers){
//            Map<String,Object> dto = new LinkedHashMap<>();
//            dto.put("workerId",worker.getId());
//            dto.put("realName",worker.getRealName());
//            dto.put("skill",worker.getSkill());
//
//            //2.统计完工数
//            Integer completedCount = repairOrderMapper.countCompletedByWorkerId(worker.getId());
//            dto.put("completedCount",completedCount!=null?completedCount:0);
//
//            //3.统计完工率
//            Integer totalReviews = evaluationMapper.selectCountByWorkerId(worker.getId());
//            dto.put("totalReviews",totalReviews!=null?totalReviews:0);
//
//            //4.统计好评数
//            Integer goodReviews = evaluationMapper
//        }
//    }
}

package com.repair.service.impl;

import com.repair.common.BusinessException;
import com.repair.entity.User;
import com.repair.mapper.UserMapper;
import com.repair.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

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

    @Override
    public User updateProfile(Integer userId, String realName, String phone) {
        //1.校验用户是否存在
        User user = getUserById(userId);

        //2.校验真实姓名
        if (realName == null || realName.trim().isEmpty()) {
            throw new BusinessException("真实姓名不能为空");
        }
        if (realName.trim().length() < 2 || realName.trim().length() > 20) {
            throw new BusinessException("真实姓名长度必须在2-20位之间");
        }

        //3.校验手机号格式
        if (phone == null || phone.trim().isEmpty()) {
            throw new BusinessException("手机号不能为空");
        }
        if (!phone.trim().matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }

        //4.更新数据库
        int rows = userMapper.updateProfile(userId, realName.trim(), phone.trim());
        if (rows <= 0) {
            throw new BusinessException("修改失败，请重试");
        }

        //5.返回最新用户信息
        return userMapper.selectById(userId);
    }

    // ==================== 用户管理（管理员） ====================

    @Override
    public Map<String, Object> listUsers(Integer role, String keyword, Integer page, Integer pageSize) {
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        int offset = (page - 1) * pageSize;

        Long total = userMapper.countPage(role, keyword);
        List<User> list = userMapper.selectPage(role, keyword, offset, pageSize);
        for (User user : list) {
            user.setPassword(null);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("list", list);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public void updateUserStatus(Integer id, Integer status) {
        if (id == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值只能为0或1");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getRole() != null && user.getRole() == 4) {
            throw new BusinessException("管理员账号不允许禁用/启用操作");
        }
        userMapper.updateStatus(id, status);
    }

    @Override
    public void resetPassword(Integer id, String newPassword) {
        if (id == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new BusinessException("新密码不能为空");
        }
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            throw new BusinessException("密码长度必须在6-20位之间");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        userMapper.updatePassword(id, newPassword);
    }

    // ==================== 维修工档案（管理员） ====================

    @Override
    public List<User> listWorkers(String skill, Integer onDuty) {
        if (onDuty != null && onDuty != 0 && onDuty != 1) {
            throw new BusinessException("在岗状态值只能为0或1");
        }
        List<User> workers = userMapper.selectWorkers(skill, onDuty);
        for (User worker : workers) {
            worker.setPassword(null);
        }
        return workers;
    }

    @Override
    public User getWorkerDetail(Integer id) {
        User user = getUserById(id);
        if (user.getRole() == null || user.getRole() != 3) {
            throw new BusinessException("该用户不是维修工");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public User updateWorkerProfile(Integer id, String skill, Integer onDuty, String serviceArea, Integer maxWorkload) {
        User user = getUserById(id);
        if (user.getRole() == null || user.getRole() != 3) {
            throw new BusinessException("该用户不是维修工");
        }
        if (onDuty != null && onDuty != 0 && onDuty != 1) {
            throw new BusinessException("在岗状态值只能为0或1");
        }
        if (maxWorkload != null && maxWorkload < 1) {
            throw new BusinessException("最大接单量不能小于1");
        }
        if (serviceArea != null && serviceArea.length() > 255) {
            throw new BusinessException("服务区域长度不能超过255位");
        }

        user.setSkill(skill != null ? skill : user.getSkill());
        Integer newOnDuty = onDuty != null ? onDuty : (user.getOnDuty() != null ? user.getOnDuty() : 1);
        user.setOnDuty(newOnDuty);
        user.setServiceArea(serviceArea != null ? serviceArea : user.getServiceArea());
        user.setMaxWorkload(maxWorkload != null ? maxWorkload : user.getMaxWorkload());
        int rows = userMapper.updateWorkerProfile(user);
        if (rows <= 0) {
            throw new BusinessException("更新失败，请重试");
        }
        User updated = userMapper.selectById(id);
        updated.setPassword(null);
        return updated;
    }
}

package com.repair.service;

import com.repair.entity.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    /**
     * 用户注册
     * @param user 用户信息
     * @return 注册成功的用户
     */
    User register(User user);

    /**
     * 用户登录
     * @param username 用户民
     * @param password 密码
     * @return 登录成功的用户
     */
    User login(String username,String password);

    /**
     *根据id查询用户
     */
    User getUserById(Integer id);

    /**
     *根据用户名查询用户
     */
    User getUserByUsername(String username);

    /**
     * 修改个人信息（手机号、真实姓名）
     * @param userId 用户ID
     * @param realName 真实姓名
     * @param phone 手机号
     * @return 修改后的用户信息
     */
    User updateProfile(Integer userId, String realName, String phone);

    // ==================== 用户管理（管理员） ====================

    /**
     * 分页查询用户列表（角色/关键字过滤），密码字段脱敏
     */
    Map<String, Object> listUsers(Integer role, String keyword, Integer page, Integer pageSize);

    /**
     * 禁用/启用账号
     */
    void updateUserStatus(Integer id, Integer status);

    /**
     * 重置密码
     */
    void resetPassword(Integer id, String newPassword);

    // ==================== 维修工档案（管理员） ====================

    /**
     * 维修工列表（技能/在岗状态过滤），密码字段脱敏
     */
    List<User> listWorkers(String skill, Integer onDuty);

    /**
     * 维修工档案详情（role=3）
     */
    User getWorkerDetail(Integer id);

    /**
     * 更新维修工档案（技能标签/在岗状态/服务区域/最大接单量）
     */
    User updateWorkerProfile(Integer id, String skill, Integer onDuty, String serviceArea, Integer maxWorkload);
}

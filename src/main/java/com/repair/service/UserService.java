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

//    /**
//     * 查询所有维修工的绩效统计
//     */
//    List<Map<String,Object>> getWorkerPerformanceList();
}

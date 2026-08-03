package com.repair.service;

import com.repair.entity.User;

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
}

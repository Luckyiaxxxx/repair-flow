package com.repair.controller.common;

import com.repair.common.Result;
import com.repair.entity.User;
import com.repair.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//用户通用接口（所有角色共用）
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    //修改个人信息（手机号、真实姓名）
    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestParam Integer userId,
                                      @RequestParam String realName,
                                      @RequestParam String phone) {
        User user = userService.updateProfile(userId, realName, phone);
        return Result.success(user);
    }
}
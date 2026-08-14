package com.repair.controller.admin;

//管理端：用户管理（禁用/启用、重置密码）

import com.repair.common.Result;
import com.repair.dto.ResetPasswordRequest;
import com.repair.entity.User;
import com.repair.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class UserManageController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Result<Map<String, Object>> listUsers(
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Map<String, Object> data = userService.listUsers(role, keyword, page, pageSize);
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Integer id) {
        User user = userService.getUserById(id);
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/{id}/status")
    public Result<String> updateUserStatus(@PathVariable Integer id, @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        return Result.success(status == 1 ? "账号已启用" : "账号已禁用");
    }

    @PutMapping("/{id}/password")
    public Result<String> resetPassword(@PathVariable Integer id, @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request.getPassword());
        return Result.success("密码重置成功");
    }
}
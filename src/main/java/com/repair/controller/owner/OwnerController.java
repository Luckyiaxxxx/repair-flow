package com.repair.controller.owner;

//业主类

import com.repair.common.Result;
import com.repair.entity.RepairOrder;
import com.repair.entity.User;
import com.repair.service.RepairOrderService;
import com.repair.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    @Autowired
    private UserService userService;

    @Autowired
    private RepairOrderService repairOrderService;

    @GetMapping("/hello")
    public Result<String> hello(){
        return Result.success("业主端测试成功");
    }

    //业主注册
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user){
        User registered = userService.register(user);
        return Result.success(registered);
    }

    //业主登录
    @PostMapping("/login")
    public Result<User> login(@RequestParam String username,@RequestParam String password){
        User user = userService.login(username,password);
        return Result.success(user);
    }

    //提交报修
    @PostMapping("/orders")
    public Result<RepairOrder> submitOrder(@RequestBody RepairOrder order,@RequestParam Integer ownerId){
        RepairOrder saved = repairOrderService.submitOrder(order,ownerId);
        return Result.success(saved);
    }

    //我的报修列表
    @GetMapping("/orders")
    public Result<List<RepairOrder>> getMyorders(@RequestParam Integer ownerId) {
        List<RepairOrder> orders = repairOrderService.getOrdersByOwnerId(ownerId);
        return Result.success(orders);
    }

    //报修详情
    @GetMapping("/orders/{orderId}")
    public Result<RepairOrder> getOrderDetail(@PathVariable Integer orderId){
        RepairOrder order  = repairOrderService.getOrderById(orderId);
        return Result.success(order);
        }

}

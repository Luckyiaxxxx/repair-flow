package com.repair.controller.owner;

//业主类

import com.repair.common.Result;
import com.repair.common.ValidationGroups;
import com.repair.entity.RepairOrder;
import com.repair.entity.User;
import com.repair.service.RepairOrderService;
import com.repair.service.UserService;
import com.repair.util.JwtUtil;
import com.repair.util.RedisUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    @Autowired
    private UserService userService;

    @Autowired
    private RepairOrderService repairOrderService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @GetMapping("/test/redis")
    public Result<String> testRedis() {
        redisUtil.set("test-key", "Hello Redis!");
        Object value = redisUtil.get("test-key");
        return Result.success("Redis 连接成功！value=" + value);
    }

    @GetMapping("/hello")
    public Result<String> hello(){
        return Result.success("业主端测试成功");
    }

    //业主注册
    @PostMapping("/register")
    public Result<User> register(@Validated(ValidationGroups.Add.class) @RequestBody User user){
        User registered = userService.register(user);
        return Result.success(registered);
    }

    //业主登录
    @PostMapping("/login")
    public Result<Map<String,Object>> login(@RequestParam String username, @RequestParam String password){
        User user = userService.login(username,password);

        //生成Token
        String token = jwtUtil.generateToken(user.getId(),user.getUsername(),user.getRole());

        //返回用户信息和Token
        Map<String,Object> data = new HashMap<>();
        data.put("user",user);
        data.put("token",token);

        return Result.success(data);
    }

    //提交报修
    @PostMapping("/orders")
    public Result<RepairOrder> submitOrder(@Validated(ValidationGroups.Add.class) @RequestBody RepairOrder order,@RequestParam Integer ownerId){
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

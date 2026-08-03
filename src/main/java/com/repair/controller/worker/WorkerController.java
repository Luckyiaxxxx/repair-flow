package com.repair.controller.worker;

import com.repair.common.Result;
import com.repair.entity.RepairOrder;
import com.repair.service.RepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//维修工端
@RestController
@RequestMapping("/api/worker")
public class WorkerController {

    @Autowired
    private RepairOrderService repairOrderService;

    @GetMapping("/hello")
    public Result<String> hello(){
        return Result.success("维修工测试成功");
    }

    //我的工单列表
    @GetMapping("/orders")
    public Result<List<RepairOrder>> getMyOrders(@RequestParam Integer workerId){
        List<RepairOrder> orders = repairOrderService.getOrdersByWorkerId(workerId);
        return Result.success(orders);
    }

    //接单
    @PutMapping("/orders/{orderId}/accept")
    public Result<String> acceptOrder(
            @PathVariable Integer orderId,
            @RequestParam Integer workerId){
        repairOrderService.acceptOrder(orderId,workerId);
        return Result.success("接单成功");
    }

    //完工
    @PutMapping("/orders/{orderId}/complete")
    public Result<String> completeOrder(
            @PathVariable Integer orderId,
            @RequestParam String repairNote,
            @RequestParam Integer repairDuration
    ){
        repairOrderService.completeOrder(orderId,repairNote,repairDuration);
        return Result.success("完工");
    }
}

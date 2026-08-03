package com.repair.controller.dispatcher;

import com.repair.common.Result;
import com.repair.entity.RepairOrder;
import com.repair.service.RepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//客服端
@RestController
@RequestMapping("/api/dispatcher")
public class DispatcherController {

    @Autowired
    private RepairOrderService repairOrderService;

    @GetMapping("/hello")
    public Result<String> hello(){
        return Result.success("客服端测试成功");
    }

    @GetMapping("/order/pending")
    public Result<List<RepairOrder>> getPendingOrders(){
        List<RepairOrder> orders = repairOrderService.getPendingOrders();
        return Result.success(orders);
    }

    //派单
    @PutMapping("/orders/{orderId}/assign")
    public Result<String> assignOrder(
        @PathVariable Integer orderId,@RequestParam Integer workerId,@RequestParam Integer dispatcherId){
        repairOrderService.assignOrder(orderId,workerId,dispatcherId);
        return Result.success("派单成功");
    }
}

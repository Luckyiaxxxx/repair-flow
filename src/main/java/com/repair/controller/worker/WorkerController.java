package com.repair.controller.worker;

import com.repair.common.Result;
import com.repair.dto.AppointmentRequest;
import com.repair.dto.CompleteOrderRequest;
import com.repair.dto.MessageRequest;
import com.repair.dto.TransferRequest;
import com.repair.entity.OrderMessage;
import com.repair.entity.OrderTransfer;
import com.repair.entity.RepairOrder;
import com.repair.service.OrderMessageService;
import com.repair.service.OrderTransferService;
import com.repair.service.RepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//维修工端
@RestController
@RequestMapping("/api/worker")
public class WorkerController {

    @Autowired
    private RepairOrderService repairOrderService;

    @Autowired
    private OrderMessageService orderMessageService;

    @Autowired
    private OrderTransferService orderTransferService;

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
            @RequestBody CompleteOrderRequest request
    ){
        repairOrderService.completeOrder(orderId, request);
        return Result.success("完工");
    }

    //维修工月度统计
    @GetMapping("/monthly-statistics")
    public Result<Map<String, Object>> getMonthlyStatistics(@RequestParam Integer workerId){
        Map<String, Object> data = repairOrderService.getWorkerMonthlyStatistics(workerId);
        return Result.success(data);
    }

    //维修工工作统计看板
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard(@RequestParam Integer workerId){
        Map<String, Object> data = repairOrderService.getWorkerDashboard(workerId);
        return Result.success(data);
    }

    // ===== A组：工单流程增强 =====

    //确认上门时间（仅维修中，本人或协助人）
    @PutMapping("/orders/{orderId}/appointment")
    public Result<String> confirmAppointment(
            @PathVariable Integer orderId,
            @RequestParam Integer workerId,
            @RequestBody AppointmentRequest request){
        repairOrderService.confirmAppointment(orderId, workerId, request.getConfirmedTime());
        return Result.success("上门时间确认成功");
    }

    //申请转单/协助（仅维修中，当前维修工）
    @PostMapping("/orders/{orderId}/transfer")
    public Result<OrderTransfer> requestTransfer(
            @PathVariable Integer orderId,
            @RequestParam Integer workerId,
            @RequestBody TransferRequest request){
        OrderTransfer transfer = orderTransferService.requestTransfer(orderId, workerId, request.getType(), request.getReason());
        return Result.success(transfer);
    }

    //我的转单/协助申请列表
    @GetMapping("/transfers")
    public Result<List<OrderTransfer>> getMyTransfers(@RequestParam Integer workerId){
        List<OrderTransfer> transfers = orderTransferService.getMyTransfers(workerId);
        return Result.success(transfers);
    }

    //发送工单留言
    @PostMapping("/orders/{orderId}/messages")
    public Result<OrderMessage> sendMessage(@PathVariable Integer orderId,
                                            @RequestParam Integer workerId,
                                            @RequestBody MessageRequest request){
        OrderMessage message = orderMessageService.sendMessage(orderId, workerId, 3, request.getContent());
        return Result.success(message);
    }

    //查看工单留言
    @GetMapping("/orders/{orderId}/messages")
    public Result<List<OrderMessage>> getMessages(@PathVariable Integer orderId, @RequestParam Integer workerId){
        List<OrderMessage> messages = orderMessageService.getMessages(orderId, workerId, 3);
        return Result.success(messages);
    }
}
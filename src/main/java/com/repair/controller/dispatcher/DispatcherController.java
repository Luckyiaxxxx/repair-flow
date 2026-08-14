package com.repair.controller.dispatcher;

import com.repair.common.Result;
import com.repair.dto.ApproveTransferRequest;
import com.repair.dto.MessageRequest;
import com.repair.dto.RejectOrderRequest;
import com.repair.dto.RejectTransferRequest;
import com.repair.entity.OrderMessage;
import com.repair.entity.OrderTransfer;
import com.repair.entity.RepairOrder;
import com.repair.entity.User;
import com.repair.service.OrderMessageService;
import com.repair.service.OrderTransferService;
import com.repair.service.RepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

//客服端
@RestController
@RequestMapping("/api/dispatcher")
public class DispatcherController {

    @Autowired
    private RepairOrderService repairOrderService;

    @Autowired
    private OrderMessageService orderMessageService;

    @Autowired
    private OrderTransferService orderTransferService;

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

    //客服工作量统计看板
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard(@RequestParam Integer dispatcherId){
        Map<String, Object> data = repairOrderService.getDispatcherDashboard(dispatcherId);
        return Result.success(data);
    }

    // ===== A组：工单流程增强 =====

    //维修工列表（派单/转派选择用，仅启用中）
    @GetMapping("/workers")
    public Result<List<User>> getWorkers(){
        List<User> workers = repairOrderService.getWorkers();
        return Result.success(workers);
    }

    //转派/改派（仅已派单/维修中）
    @PutMapping("/orders/{orderId}/reassign")
    public Result<String> reassignOrder(
            @PathVariable Integer orderId,
            @RequestParam Integer newWorkerId,
            @RequestParam Integer dispatcherId){
        repairOrderService.reassignOrder(orderId, newWorkerId, dispatcherId, 1);
        return Result.success("转派成功");
    }

    //驳回无效工单（仅待派单，需驳回原因）
    @PutMapping("/orders/{orderId}/reject")
    public Result<String> rejectOrder(
            @PathVariable Integer orderId,
            @RequestParam Integer dispatcherId,
            @RequestBody RejectOrderRequest request){
        repairOrderService.rejectOrder(orderId, dispatcherId, request.getReason());
        return Result.success("驳回成功");
    }

    //发送工单留言
    @PostMapping("/orders/{orderId}/messages")
    public Result<OrderMessage> sendMessage(@PathVariable Integer orderId,
                                            @RequestParam Integer dispatcherId,
                                            @RequestBody MessageRequest request){
        OrderMessage message = orderMessageService.sendMessage(orderId, dispatcherId, 2, request.getContent());
        return Result.success(message);
    }

    //查看工单留言
    @GetMapping("/orders/{orderId}/messages")
    public Result<List<OrderMessage>> getMessages(@PathVariable Integer orderId, @RequestParam Integer dispatcherId){
        List<OrderMessage> messages = orderMessageService.getMessages(orderId, dispatcherId, 2);
        return Result.success(messages);
    }

    //待处理的转单/协助申请列表
    @GetMapping("/transfers")
    public Result<List<OrderTransfer>> getPendingTransfers(){
        List<OrderTransfer> transfers = orderTransferService.getPendingTransfers();
        return Result.success(transfers);
    }

    //同意转单/协助申请
    @PutMapping("/transfers/{id}/approve")
    public Result<String> approveTransfer(@PathVariable Integer id,
                                          @RequestParam Integer dispatcherId,
                                          @RequestBody ApproveTransferRequest request){
        orderTransferService.approveTransfer(id, dispatcherId, request.getNewWorkerId());
        return Result.success("已同意");
    }

    //拒绝转单/协助申请
    @PutMapping("/transfers/{id}/reject")
    public Result<String> rejectTransfer(@PathVariable Integer id,
                                         @RequestParam Integer dispatcherId,
                                         @RequestBody RejectTransferRequest request){
        orderTransferService.rejectTransfer(id, dispatcherId, request.getNote());
        return Result.success("已拒绝");
    }
}
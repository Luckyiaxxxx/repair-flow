package com.repair.controller.admin;

//管理端

import com.repair.common.Result;
import com.repair.common.ValidationGroups;
import com.repair.entity.Announcement;
import com.repair.entity.Feedback;
import com.repair.entity.Material;
import com.repair.service.AnnouncementService;
import com.repair.service.DashboardService;
import com.repair.service.FeedbackService;
import com.repair.service.MaterialService;
import com.repair.service.RepairOrderService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {


    @Autowired
    private MaterialService materialService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private RepairOrderService repairOrderService;

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/hello")
    public Result<String> hello(){
        return Result.success("管理端测试成功");
    }

    @PostMapping("/materials")
    public Result<Material> addMaterial(@Validated(ValidationGroups.Add.class)@RequestBody Material material){
        Material saved = materialService.addMaterial(material);
        return Result.success(saved);
    }

    @DeleteMapping("/materials/{id}")
    public Result<String> deleteMaterial(@PathVariable Integer id){
        materialService.deleteMaterial(id);
        return Result.success("删除成功");
    }

    @PutMapping("/materials")
    public Result<Material> updateMaterial(@Validated(ValidationGroups.Update.class) @RequestBody Material material){
        Material updated = materialService.updateMaterial(material);
        return Result.success(updated);
    }

    @GetMapping("/materials/{id}")
    public Result<Material> getMaterialById(@PathVariable Integer id){
        Material material = materialService.getMaterialById(id);
        return Result.success(material);
    }

    @GetMapping("/materials")
    public Result<List<Material>> listAllmaterials() {
        List<Material> materials = materialService.listAllMaterials();
        return Result.success(materials);
    }

    @GetMapping("/materials/low-stock")
    public Result<List<Material>> getLowStockMaterials(){
        List<Material> materials = materialService.getLowStockMaterials();
        return Result.success(materials);
    }

    @PutMapping("/materials/{id}/add-stock")
    public Result<String> addStock(@PathVariable Integer id ,@RequestParam Integer quantity){
        materialService.addStock(id,quantity);
        return Result.success("入库成功，增加"+quantity+"件");
    }

    @PutMapping("/materials/{id}/deduct-stock")
    public Result<String> deductStock(@PathVariable Integer id,@RequestParam Integer quantity){
        materialService.deductStock(id,quantity);
        return Result.success("出库成功，扣减"+quantity+"件");
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> data = dashboardService.getDashboardData();
        return Result.success(data);
    }

    @GetMapping("/orders/search")
    public Result<Map<String, Object>> searchOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize){
        Map<String, Object> data = repairOrderService.searchOrders(status, category, building, startDate, endDate, page, pageSize);
        return Result.success(data);
    }

    // ==================== 公告管理 ====================

    @PostMapping("/announcements")
    public Result<Announcement> addAnnouncement(@RequestBody Announcement announcement) {
        Announcement saved = announcementService.addAnnouncement(announcement);
        return Result.success(saved);
    }

    @DeleteMapping("/announcements/{id}")
    public Result<String> deleteAnnouncement(@PathVariable Integer id) {
        announcementService.deleteAnnouncement(id);
        return Result.success("删除成功");
    }

    @PutMapping("/announcements")
    public Result<Announcement> updateAnnouncement(@RequestBody Announcement announcement) {
        Announcement updated = announcementService.updateAnnouncement(announcement);
        return Result.success(updated);
    }

    @GetMapping("/announcements/{id}")
    public Result<Announcement> getAnnouncementById(@PathVariable Integer id) {
        Announcement announcement = announcementService.getAnnouncementById(id);
        return Result.success(announcement);
    }

    @GetMapping("/announcements")
    public Result<List<Announcement>> listAllAnnouncements() {
        List<Announcement> announcements = announcementService.listAllAnnouncements();
        return Result.success(announcements);
    }

    // ==================== 投诉建议管理 ====================

    @GetMapping("/feedbacks")
    public Result<List<Feedback>> listAllFeedbacks() {
        List<Feedback> feedbacks = feedbackService.listAllFeedbacks();
        return Result.success(feedbacks);
    }

    @GetMapping("/feedbacks/{id}")
    public Result<Feedback> getFeedbackById(@PathVariable Integer id) {
        Feedback feedback = feedbackService.getFeedbackById(id);
        return Result.success(feedback);
    }

    @PutMapping("/feedbacks/{id}/reply")
    public Result<String> replyFeedback(@PathVariable Integer id, @RequestParam String reply) {
        feedbackService.replyFeedback(id, reply);
        return Result.success("回复成功");
    }

    @DeleteMapping("/feedbacks/{id}")
    public Result<String> deleteFeedback(@PathVariable Integer id) {
        feedbackService.deleteFeedback(id);
        return Result.success("删除成功");
    }
}

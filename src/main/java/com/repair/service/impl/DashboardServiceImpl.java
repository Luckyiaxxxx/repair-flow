package com.repair.service.impl;

import com.repair.mapper.RepairOrderMapper;
import com.repair.mapper.UserMapper;
import com.repair.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new LinkedHashMap<>();

        // 1. 核心指标
        Long totalUsers = userMapper.countAll();
        data.put("总用户数", totalUsers != null ? totalUsers : 0L);

        Long totalOrders = repairOrderMapper.countAll();
        data.put("总报修数", totalOrders != null ? totalOrders : 0L);

        Long pendingOrders = repairOrderMapper.countByStatus(1);
        data.put("待派单", pendingOrders != null ? pendingOrders : 0L);

        Long inProgressOrders = repairOrderMapper.countByStatus(3);
        data.put("维修中", inProgressOrders != null ? inProgressOrders : 0L);

        Long completedOrders = repairOrderMapper.countByStatus(4);
        data.put("已完工", completedOrders != null ? completedOrders : 0L);

        Long evaluatedOrders = repairOrderMapper.countByStatus(5);
        data.put("已评价", evaluatedOrders != null ? evaluatedOrders : 0L);

        // 2. 完工率
        if (totalOrders != null && totalOrders > 0) {
            double rate = (double) (completedOrders != null ? completedOrders : 0) / totalOrders * 100;
            data.put("完工率", Math.round(rate * 10) / 10.0 + "%");
        } else {
            data.put("完工率", "0%");
        }

        // 3. 近7天趋势
        List<Map<String, Object>> trendData = repairOrderMapper.selectTrend(7);
        Map<String, Long> trendMap = trendData.stream()
                .collect(Collectors.toMap(
                        m -> m.get("date").toString(),
                        m -> ((Number) m.get("count")).longValue()
                ));

        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        for (int i = 6; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).format(formatter);
            dates.add(date);
            counts.add(trendMap.getOrDefault(date, 0L));
        }

        data.put("趋势日期", dates);
        data.put("趋势数量", counts);

        // 4. 各状态占比
        List<Map<String, Object>> statusData = repairOrderMapper.countByStatusGroup();
        Map<String, Long> statusMap = new LinkedHashMap<>();
        for (Map<String, Object> row : statusData) {
            Integer status = (Integer) row.get("status");
            Long count = ((Number) row.get("count")).longValue();
            statusMap.put(getStatusName(status), count);
        }
        data.put("状态分布", statusMap);

        // 5. 各类别占比
        List<Map<String, Object>> categoryData = repairOrderMapper.countByCategory();
        Map<String, Long> categoryMap = new LinkedHashMap<>();
        for (Map<String, Object> row : categoryData) {
            String category = row.get("category").toString();
            Long count = ((Number) row.get("count")).longValue();
            categoryMap.put(category, count);
        }
        data.put("类别分布", categoryMap);

        return data;
    }

    private String getStatusName(Integer status) {
        switch (status) {
            case 1: return "待派单";
            case 2: return "已派单";
            case 3: return "维修中";
            case 4: return "已完工";
            case 5: return "已评价";
            default: return "未知";
        }
    }
}
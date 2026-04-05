package com.office.newofficeautomationbackend.service.impl;

import com.office.newofficeautomationbackend.dto.DashboardDTO;
import com.office.newofficeautomationbackend.mapper.StatsMapper;
import com.office.newofficeautomationbackend.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 全系统统计业务逻辑实现类
 */
@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    private StatsMapper statsMapper;

    @Override
    public DashboardDTO getDashboardData() {
        DashboardDTO dashboard = new DashboardDTO();

        // 1. 获取基础统计 (用户/部门)
        dashboard.setSystemStats(statsMapper.getSystemStats());

        // 2. 获取文件库统计
        Map<String, Object> fileStatsRaw = statsMapper.getFileStats();
        Map<String, Object> fileStats = new HashMap<>(fileStatsRaw);
        
        // 健壮性处理：将总空间占用换算为 MB 格式
        Object totalSizeRaw = fileStatsRaw.get("totalSize");
        long totalSizeInBytes = convertToLong(totalSizeRaw);
        double totalSizeInMB = totalSizeInBytes / (1024.0 * 1024.0);
        fileStats.put("totalSizeMB", String.format("%.2f MB", totalSizeInMB));
        dashboard.setFileStats(fileStats);

        // 3. 获取通知公告统计
        dashboard.setNoticeStats(statsMapper.getNoticeStats());

        // 4. 处理公文状态分布
        Map<Integer, Map<String, Object>> docStatsRaw = statsMapper.getDocumentStatusStats();
        Map<Integer, Object> docStats = new HashMap<>();
        docStatsRaw.forEach((status, data) -> {
            docStats.put(status, data.get("count"));
        });
        dashboard.setDocumentStats(docStats);

        // 5. 处理日程状态分布
        Map<Integer, Map<String, Object>> schStatsRaw = statsMapper.getScheduleStatusStats();
        Map<Integer, Object> schStats = new HashMap<>();
        schStatsRaw.forEach((status, data) -> {
            schStats.put(status, data.get("count"));
        });
        dashboard.setScheduleStats(schStats);

        return dashboard;
    }

    /**
     * 辅助工具：安全地将数据库聚合结果对象转换为 Long
     */
    private long convertToLong(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        return 0L;
    }
}

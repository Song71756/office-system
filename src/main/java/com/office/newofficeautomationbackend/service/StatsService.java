package com.office.newofficeautomationbackend.service;

import com.office.newofficeautomationbackend.dto.DashboardDTO;

/**
 * 全系统统计业务逻辑接口
 */
public interface StatsService {
    /**
     * 获取看板核心指标统计数据
     * @return 包含全系统运行状态的 DTO
     */
    DashboardDTO getDashboardData();
}

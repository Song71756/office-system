package com.office.newofficeautomationbackend.controller;

import com.office.newofficeautomationbackend.common.Result;
import com.office.newofficeautomationbackend.common.annotation.CheckPermission;
import com.office.newofficeautomationbackend.dto.DashboardDTO;
import com.office.newofficeautomationbackend.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 全系统统计看板控制层
 */
@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    /**
     * 获取首页看板全量统计数据
     * 权限要求：stats:view
     */
    @GetMapping("/dashboard")
    @CheckPermission("stats:view")
    public Result<DashboardDTO> getDashboard() {
        return Result.success("统计数据获取成功", statsService.getDashboardData());
    }
}

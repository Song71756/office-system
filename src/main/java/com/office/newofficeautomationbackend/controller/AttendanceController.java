package com.office.newofficeautomationbackend.controller;

import com.github.pagehelper.PageInfo;
import com.office.newofficeautomationbackend.common.Result;
import com.office.newofficeautomationbackend.common.annotation.CheckPermission;
import com.office.newofficeautomationbackend.common.annotation.Logical;
import com.office.newofficeautomationbackend.entity.Attendance;
import com.office.newofficeautomationbackend.service.AttendanceService;
import com.office.newofficeautomationbackend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 考勤管理控制层
 * 提供：打卡、历史查询、月度统计等接口
 */
@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 核心打卡接口（自动判断签到/签退）
     * 权限：attendance:punch（若暂未配置权限，可先放行，后续再加）
     * Body(JSON)：{"location": "北京总部大楼", "ipAddress": "192.168.1.10"}
     */
    @PostMapping("/punch")
    @CheckPermission(value = {"attendance:punch", "attendance:edit"}, logical = Logical.OR)
    public Result<Attendance> punch(@RequestBody(required = false) Map<String, String> body,
                                    @RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUsernameFromToken(token);
        String location = body != null ? body.getOrDefault("location", null) : null;
        String ip = body != null ? body.getOrDefault("ipAddress", null) : null;
        Attendance saved = attendanceService.punchCard(username, ip, location);
        return Result.success("打卡成功", saved);
    }

    /**
     * 分页获取我的考勤历史
     * 权限：attendance:view
     */
    @GetMapping("/myHistory")
    @CheckPermission("attendance:view")
    public Result<PageInfo<Attendance>> myHistory(@RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                                  @RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUsernameFromToken(token);
        return Result.success("查询成功", attendanceService.findMyHistory(pageNum, pageSize, username));
    }

    /**
     * 获取我在某年某月的考勤统计（状态分布）
     * 权限：attendance:view
     */
    @GetMapping("/myMonthStats")
    @CheckPermission("attendance:view")
    public Result<Map<String, Object>> myMonthStats(@RequestParam Integer year,
                                                    @RequestParam Integer month,
                                                    @RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUsernameFromToken(token);
        return Result.success("统计成功", attendanceService.getMyMonthStats(username, year, month));
    }
}

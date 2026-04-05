package com.office.newofficeautomationbackend.service;

import com.github.pagehelper.PageInfo;
import com.office.newofficeautomationbackend.entity.Attendance;
import java.util.Map;

/**
 * 考勤管理业务逻辑接口
 */
public interface AttendanceService {
    /**
     * 核心打卡逻辑：自动判断签到或签退
     * @param username 当前操作用户名
     * @param ipAddress 客户端 IP
     * @param location 打卡位置
     * @return 最新的考勤记录实体
     */
    Attendance punchCard(String username, String ipAddress, String location);

    /**
     * 分页获取个人考勤历史
     */
    PageInfo<Attendance> findMyHistory(int pageNum, int pageSize, String username);

    /**
     * 获取指定月份的个人考勤统计
     * @param username 用户名
     * @param year 年份
     * @param month 月份
     * @return 状态分布统计结果
     */
    Map<String, Object> getMyMonthStats(String username, int year, int month);
}

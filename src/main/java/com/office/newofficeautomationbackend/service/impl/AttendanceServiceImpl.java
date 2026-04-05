package com.office.newofficeautomationbackend.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.office.newofficeautomationbackend.dto.UserDTO;
import com.office.newofficeautomationbackend.entity.Attendance;
import com.office.newofficeautomationbackend.entity.User;
import com.office.newofficeautomationbackend.mapper.AttendanceMapper;
import com.office.newofficeautomationbackend.service.AttendanceService;
import com.office.newofficeautomationbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 考勤管理业务实现类
 * 提供“签到/签退”打卡、历史查询与月度统计能力
 */
@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private UserService userService;

    /**
     * 约定的上/下班时间（可后续抽到配置）
     */
    private static final LocalTime WORK_START = LocalTime.of(9, 0, 0);   // 09:00:00
    private static final LocalTime WORK_END = LocalTime.of(18, 0, 0);    // 18:00:00

    /**
     * 核心打卡逻辑：
     * 1. 当日无记录 -> 视为“签到”：落库 signInTime；根据是否超过上班时间判定是否迟到。
     * 2. 当日已有记录且无 signOutTime -> 视为“签退”：写入 signOutTime；结合上下班时间计算最终状态（早退/正常/迟到）。
     */
    @Override
    public Attendance punchCard(String username, String ipAddress, String location) {
        // 1. 获取当前用户
        UserDTO userDTO = userService.getByUsername(username);
        if (userDTO == null) {
            throw new RuntimeException("当前用户不存在，无法打卡");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // 2. 查询当日是否已存在考勤记录
        Attendance record = attendanceMapper.getByUserAndDate(userDTO.getId(), today);

        if (record == null) {
            // 2.1 首次打卡 -> 签到
            record = new Attendance();
            record.setUserId(userDTO.getId());
            record.setAttendanceDate(today);
            record.setSignInTime(now);
            record.setIpAddress(ipAddress);
            record.setLocation(location);
            record.setCreateTime(now);
            record.setUpdateTime(now);

            // 判定是否迟到：签到时间是否超过 09:00
            if (now.toLocalTime().isAfter(WORK_START)) {
                record.setStatus(2); // 迟到
                record.setRemark("签到超过上班时间，标记为迟到");
            } else {
                record.setStatus(1); // 正常
            }

            attendanceMapper.insert(record);
            return record;
        }

        // 2.2 二次打卡 -> 签退（仅当尚未签退时有效）
        if (record.getSignOutTime() == null) {
            record.setSignOutTime(now);
            record.setUpdateTime(now);

            // 计算最终状态：优先判断早退；否则保持迟到或正常
            boolean isEarlyLeave = now.toLocalTime().isBefore(WORK_END);
            boolean wasLate = record.getSignInTime() != null && record.getSignInTime().toLocalTime().isAfter(WORK_START);

            if (isEarlyLeave) {
                record.setStatus(3); // 早退（若既迟到又早退，最终以早退为准）
                if (record.getRemark() == null) record.setRemark("签退早于下班时间，标记为早退");
            } else if (wasLate) {
                record.setStatus(2); // 保持迟到
            } else {
                record.setStatus(1); // 正常
            }

            attendanceMapper.updateSignOut(record);
            return record;
        }

        // 2.3 已有签到与签退
        throw new RuntimeException("今日已完成签到与签退，无需重复打卡");
    }

    /**
     * 分页查询个人考勤历史（按日期倒序）
     */
    @Override
    public PageInfo<Attendance> findMyHistory(int pageNum, int pageSize, String username) {
        UserDTO userDTO = userService.getByUsername(username);
        if (userDTO == null) {
            throw new RuntimeException("当前用户不存在");
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Attendance> list = attendanceMapper.selectMyHistory(userDTO.getId());
        return new PageInfo<>(list);
    }

    /**
     * 获取个人某年月的考勤状态分布
     */
    @Override
    public Map<String, Object> getMyMonthStats(String username, int year, int month) {
        UserDTO userDTO = userService.getByUsername(username);
        if (userDTO == null) {
            throw new RuntimeException("当前用户不存在");
        }
        // 形如：2026-03%
        String monthPrefix = String.format("%04d-%02d%%", year, month);
        Map<Integer, Map<String, Object>> raw = attendanceMapper.getMonthStats(userDTO.getId(), monthPrefix);

        // 扁平化结果：{"1": 正常数, "2": 迟到数, ...}
        Map<String, Object> result = new HashMap<>();
        raw.forEach((status, data) -> result.put(String.valueOf(status), data.get("count")));
        return result;
    }
}

package com.office.newofficeautomationbackend.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤管理实体类
 * 对应数据库表：oa_attendance
 * 用于记录员工的每日打卡状态、时间及地点信息
 */
@Data
public class Attendance {
    /**
     * 考勤记录唯一标识 ID
     */
    private Integer id;

    /**
     * 员工唯一标识 ID (关联 sys_user.id)
     */
    private Integer userId;

    /**
     * 考勤日期 (格式：yyyy-MM-dd)
     */
    private LocalDate attendanceDate;

    /**
     * 上班签到具体时间
     */
    private LocalDateTime signInTime;

    /**
     * 下班签退具体时间
     */
    private LocalDateTime signOutTime;

    /**
     * 考勤状态 (1:正常, 2:迟到, 3:早退, 4:缺卡, 5:请假)
     */
    private Integer status;

    /**
     * 打卡时的客户端 IP 地址
     */
    private String ipAddress;

    /**
     * 打卡时的地理位置描述
     */
    private String location;

    /**
     * 考勤备注 (如：补卡说明或迟到理由)
     */
    private String remark;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;

    /**
     * 记录最后修改时间
     */
    private LocalDateTime updateTime;
}

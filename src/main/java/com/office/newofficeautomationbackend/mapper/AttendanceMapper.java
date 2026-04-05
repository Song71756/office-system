package com.office.newofficeautomationbackend.mapper;

import com.office.newofficeautomationbackend.entity.Attendance;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤管理持久层接口
 * 负责 oa_attendance 表的数据库交互
 */
@Mapper
public interface AttendanceMapper {

    /**
     * 根据用户 ID 和日期获取考勤记录
     * @param userId 用户 ID
     * @param date 考勤日期
     * @return 考勤实体
     */
    @Select("SELECT * FROM oa_attendance WHERE user_id = #{userId} AND attendance_date = #{date}")
    Attendance getByUserAndDate(@Param("userId") Integer userId, @Param("date") LocalDate date);

    /**
     * 插入新的考勤记录 (签到)
     */
    @Insert("INSERT INTO oa_attendance (user_id, attendance_date, sign_in_time, status, ip_address, location, remark, create_time, update_time) " +
            "VALUES (#{userId}, #{attendanceDate}, #{signInTime}, #{status}, #{ipAddress}, #{location}, #{remark}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Attendance attendance);

    /**
     * 更新考勤记录 (签退)
     */
    @Update("UPDATE oa_attendance SET sign_out_time=#{signOutTime}, status=#{status}, update_time=#{updateTime} WHERE id=#{id}")
    int updateSignOut(Attendance attendance);

    /**
     * 分页查询个人考勤历史
     * @param userId 用户 ID
     * @return 考勤列表
     */
    @Select("SELECT * FROM oa_attendance WHERE user_id = #{userId} ORDER BY attendance_date DESC")
    List<Attendance> selectMyHistory(@Param("userId") Integer userId);

    /**
     * 统计指定月份的考勤状态分布
     * @param userId 用户 ID
     * @param monthStr 月份前缀 (如 '2026-03%')
     * @return 状态统计结果集
     */
    @Select("SELECT status, COUNT(*) as count FROM oa_attendance " +
            "WHERE user_id = #{userId} AND attendance_date LIKE #{monthStr} " +
            "GROUP BY status")
    @MapKey("status")
    java.util.Map<Integer, java.util.Map<String, Object>> getMonthStats(@Param("userId") Integer userId, @Param("monthStr") String monthStr);
}

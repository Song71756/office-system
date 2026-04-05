package com.office.newofficeautomationbackend.mapper;

import com.office.newofficeautomationbackend.entity.Schedule;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 日程管理持久层接口
 * 负责 oa_schedule 表的数据交互
 */
@Mapper
public interface ScheduleMapper {

    /**
     * 安全分页查询日程列表
     * 逻辑说明：
     * 1. 用户本人可以看到自己创建的所有日程 (即 user_id = #{userId})。
     * 2. 用户可以看到参与人中包含自己用户名或真实姓名的所有日程。
     * @param userId 当前登录用户 ID
     * @param username 当前登录用户名
     * @param realName 当前登录用户真实姓名
     * @param keyword 标题模糊搜索
     * @param status 状态过滤
     * @return 日程列表
     */
    @Select("<script>" +
            "SELECT * FROM oa_schedule WHERE (user_id = #{userId} OR participants LIKE CONCAT('%', #{username}, '%') OR participants LIKE CONCAT('%', #{realName}, '%')) " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND title LIKE CONCAT('%', #{keyword}, '%') " +
            "</if>" +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "ORDER BY start_time ASC" +
            "</script>")
    List<Schedule> selectPage(@Param("userId") Integer userId,
                              @Param("username") String username,
                              @Param("realName") String realName,
                              @Param("keyword") String keyword, 
                              @Param("status") Integer status);

    /**
     * 分页查询全部日程列表（不限用户）
     * @param keyword 标题模糊搜索
     * @param status 状态过滤
     * @return 日程列表
     */
    @Select("<script>" +
            "SELECT * FROM oa_schedule WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND title LIKE CONCAT('%', #{keyword}, '%') " +
            "</if>" +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "ORDER BY start_time ASC" +
            "</script>")
    List<Schedule> selectAllPage(@Param("keyword") String keyword,
                                 @Param("status") Integer status);

    /**
     * 根据 ID 获取日程详情
     */
    @Select("SELECT * FROM oa_schedule WHERE id = #{id}")
    Schedule getById(Integer id);

    /**
     * 插入新日程
     */
    @Insert("INSERT INTO oa_schedule (title, content, type, start_time, end_time, user_id, priority, remind_type, status, is_all_day, location, participants, create_time, update_time) " +
            "VALUES (#{title}, #{content}, #{type}, #{startTime}, #{endTime}, #{userId}, #{priority}, #{remindType}, #{status}, #{isAllDay}, #{location}, #{participants}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Schedule schedule);

    /**
     * 更新日程
     */
    @Update("UPDATE oa_schedule SET title=#{title}, content=#{content}, type=#{type}, start_time=#{startTime}, " +
            "end_time=#{endTime}, priority=#{priority}, remind_type=#{remindType}, status=#{status}, " +
            "is_all_day=#{isAllDay}, location=#{location}, participants=#{participants}, update_time=#{updateTime} WHERE id=#{id}")
    int update(Schedule schedule);

    /**
     * 删除日程
     */
    @Delete("DELETE FROM oa_schedule WHERE id = #{id}")
    int deleteById(Integer id);
}

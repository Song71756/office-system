package com.office.newofficeautomationbackend.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 全系统统计持久层接口
 * 负责从各个业务表中提取聚合指标，用于首页看板展示
 */
@Mapper
public interface StatsMapper {

    /**
     * 统计系统基础数据：用户总数和部门总数
     */
    @Select("SELECT " +
            "(SELECT COUNT(*) FROM sys_user) as userCount, " +
            "(SELECT COUNT(*) FROM sys_department) as deptCount")
    Map<String, Object> getSystemStats();

    /**
     * 按状态统计公文数量分布
     * 返回结果：Map<status, count>
     */
    @MapKey("status")
    @Select("SELECT status, COUNT(*) as count FROM oa_document GROUP BY status")
    Map<Integer, Map<String, Object>> getDocumentStatusStats();

    /**
     * 按状态统计日程完成情况
     */
    @MapKey("status")
    @Select("SELECT status, COUNT(*) as count FROM oa_schedule GROUP BY status")
    Map<Integer, Map<String, Object>> getScheduleStatusStats();

    /**
     * 统计文件库指标：文件数、文件夹数、总空间占用
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN file_type = 'folder' THEN 1 END) as folderCount, " +
            "COUNT(CASE WHEN file_type != 'folder' THEN 1 END) as fileCount, " +
            "IFNULL(SUM(file_size), 0) as totalSize " +
            "FROM oa_file")
    Map<String, Object> getFileStats();

    /**
     * 统计公告指标：已发布公告数和累计阅读量
     */
    @Select("SELECT " +
            "COUNT(*) as totalPublished, " +
            "IFNULL(SUM(view_count), 0) as totalViews " +
            "FROM oa_notice WHERE status = 1")
    Map<String, Object> getNoticeStats();
}

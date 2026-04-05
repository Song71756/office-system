package com.office.newofficeautomationbackend.dto;

import lombok.Data;
import java.util.Map;

/**
 * 首页看板统计数据传输对象
 * 用于封装全系统的核心运行指标
 */
@Data
public class DashboardDTO {
    /**
     * 系统基础统计：用户总数、部门总数等
     */
    private Map<String, Object> systemStats;

    /**
     * 公文统计：各状态公文的数量分布 (草稿、审核中、已通过、已驳回)
     */
    private Map<Integer, Object> documentStats;

    /**
     * 日程统计：个人及公共日程总数，或按状态分布
     */
    private Map<Integer, Object> scheduleStats;

    /**
     * 文件统计：文件总数、文件夹总数、总存储空间占用
     */
    private Map<String, Object> fileStats;

    /**
     * 公告统计：已发布的公告总数、累计阅读量
     */
    private Map<String, Object> noticeStats;
}

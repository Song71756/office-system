package com.office.newofficeautomationbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;


/**
 * 日程管理实体类
 * 对应数据库表：oa_schedule
 * 用于记录员工的工作计划、会议安排及提醒
 */
@Data
public class Schedule {
    /**
     * 日程唯一标识 ID
     */
    private Integer id;

    /**
     * 日程标题
     */
    private String title;

    /**
     * 日程详细内容或描述
     */
    private String content;

    /**
     * 日程类型 (如：1:工作, 2:会议, 3:个人, 4:其他)
     */
    private String type;

    /**
     * 日程开始时间
     */
    private LocalDateTime startTime;

    /**
     * 日程结束时间
     */
    private LocalDateTime endTime;

    /**
     * 所属用户 ID (日程的所有者)
     */
    private Integer userId;

    /**
     * 日程优先级 (如：L:低, M:中, H:高)
     */
    private String priority;

    /**
     * 提醒类型 (0:不提醒, 1:提前5分钟, 2:提前15分钟, 3:提前1小时, 4:准时提醒)
     */
    private Integer remindType;

    /**
     * 日程状态 (0:未开始, 1:进行中, 2:已完成, 3:已取消)
     */
    private Integer status;

    /**
     * 是否全天日程 (0:否, 1:是)
     */
    private Integer isAllDay;

    /**
     * 日程地点
     */
    private String location;

    /**
     * 参与人列表 (可存储多个用户姓名或 ID 的字符串，如 "张三,李四")
     */
    private String participants;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;

    /**
     * 记录最后修改时间
     */
    private LocalDateTime updateTime;

}

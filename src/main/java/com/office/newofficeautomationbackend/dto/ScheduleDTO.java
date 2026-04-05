package com.office.newofficeautomationbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日程数据传输对象
 * 用于向前端返回日程信息，包含创建人的用户名和真实姓名等非数据库字段
 */
@Data
public class ScheduleDTO {
    private Integer id;
    private String title;
    private String content;
    private String type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer userId;
    private String priority;
    private Integer remindType;
    private Integer status;
    private Integer isAllDay;
    private String location;
    private String participants;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 创建人用户名（通过 userId 查询填充）
     */
    private String username;

    /**
     * 创建人真实姓名（通过 userId 查询填充）
     */
    private String realName;
}

package com.office.newofficeautomationbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知公告实体类
 * 对应数据库中的通知表，用于系统消息发布、政策传达等
 */
@Data
public class Notice {
    /**
     * 通知唯一标识 ID
     */
    private Integer id;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知正文内容 (支持富文本或纯文本)
     */
    private String content;

    /**
     * 通知类型 (如 1:全体公告, 2:部门通知, 3:会议通知)
     */
    private Integer type;

    /**
     * 通知状态 (0:草稿, 1:已发布, 2:已撤回)
     */
    private Integer status;

    /**
     * 优先级 (如 L:普通, M:重要, H:紧急)
     */
    private String priority;

    /**
     * 发布人用户 ID
     */
    private Integer publisherId;

    /**
     * 最终确认发布的时间
     */
    private LocalDateTime publishTime;

    /**
     * 被阅读的总次数
     */
    private Integer viewCount;

    /**
     * 附件原始名称 (如有)
     */
    private String attachmentName;

    /**
     * 附件新名称 (如上传后重命名)
     */
    private String newFileName;

    /**
     * 附件在服务器上的存储路径
     */
    private String attachmentPath;

    /**
     * 通知展示的过期时间 (超过此时间可自动隐藏)
     */
    private LocalDateTime endTime;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;

    /**
     * 记录最后修改时间
     */
    private LocalDateTime updateTime;
}

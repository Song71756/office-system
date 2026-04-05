package com.office.newofficeautomationbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公文管理实体类
 * 对应数据库表：oa_document
 * 记录公文的流转、状态、审批及关联附件信息
 */
@Data
public class Document {
    /**
     * 公文唯一标识 ID
     */
    private Integer id;

    /**
     * 公文编号 (系统自动生成或手动录入，如：[2026]001号)
     */
    private String docNumber;

    /**
     * 公文标题
     */
    private String title;

    /**
     * 公文正文内容 (支持存储富文本或大段文本)
     */
    private String content;

    /**
     * 公文类型 (如：通知、请示、函、报告等)
     */
    private String type;

    /**
     * 所属部门 ID (发起公文的部门)
     */
    private Integer departmentId;

    /**
     * 关联的文件 ID (对应 oa_file 表，作为公文的电子附件)
     */
    private Integer fileId;

    /**
     * 公文优先级 (1:普通, 2:重要, 3:紧急)
     */
    private Integer priority;

    /**
     * 公文状态 (0:草稿, 1:审核中, 2:已通过, 3:驳回)
     */
    private Integer status;

    /**
     * 创建人 (发起人) 用户 ID
     */
    private Integer creatorId;

    /**
     * 审批人用户 ID
     */
    private Integer approverId;

    /**
     * 审批意见或评论
     */
    private String approveComment;

    /**
     * 审批完成的时间
     */
    private LocalDateTime approveTime;

    /**
     * 公文创建时间
     */
    private LocalDateTime createTime;

    /**
     * 记录最后修改时间
     */
    private LocalDateTime updateTime;

}

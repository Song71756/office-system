package com.office.newofficeautomationbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;


/**
 * 角色实体类
 * 对应数据库中的角色表，用于定义系统中不同的职能岗位（如管理员、普通员工等）
 */
@Data
public class Role {
    /**
     * 角色唯一标识 ID
     */
    private Integer id;

    /**
     * 角色编码 (如 ROLE_ADMIN, ROLE_USER)
     * 用于在代码或 Security 框架中进行权限判定
     */
    private String roleCode;

    /**
     * 角色展示名称 (如 系统管理员, 研发人员)
     */
    private String roleName;

    /**
     * 角色的详细描述信息
     */
    private String description;

    /**
     * 角色记录创建时间
     */
    private LocalDateTime createTime;

    /**
     * 角色记录最后更新时间
     */
    private LocalDateTime updateTime;
}

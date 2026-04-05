package com.office.newofficeautomationbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门实体类
 * 对应数据库中的部门表，支持树形层级结构
 */
@Data
public class Department {
    /**
     * 部门唯一标识 ID
     */
    private Integer id;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 部门职能或详细描述
     */
    private String description;

    /**
     * 父级部门 ID (顶层部门该值为 0 或 null)
     */
    private Integer parentId;

    /**
     * 在同级部门中的显示排序权重 (数值越小越靠前)
     */
    private Integer orderNum;

    /**
     * 部门创建时间
     */
    private LocalDateTime createTime;

    /**
     * 部门资料最后更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 子部门列表
     * 该字段仅用于在业务层构建树形结构返回给前端，不对应数据库字段
     */
    private List<Department> children;
}

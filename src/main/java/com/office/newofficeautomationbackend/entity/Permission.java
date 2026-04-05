package com.office.newofficeautomationbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;
/**
 * 权限实体类
 * 对应数据库中的权限表，定义了系统中具体的资源访问控制项（如菜单、按钮、API 路径等）
 */
@Data
public class Permission {
    /**
     * 权限唯一标识 ID
     */
    private Integer id;

    /**
     * 权限编码 (如 user:add, file:delete)
     * 用于在拦截器或 Service 层进行细粒度的授权校验
     */
    private String permissionCode;

    /**
     * 权限展示名称 (如 新增用户, 导出报表)
     */
    private String permissionName;

    /**
     * 权限类型 (如 M:目录, C:菜单, F:按钮)
     */
    private String type;

    /**
     * 父权限 ID (用于构建树形菜单结构)
     */
    private Integer parentId;

    /**
     * 权限对应的路由路径或 API 地址
     */
    private String path;

    /**
     * 权限展示时使用的图标名
     */
    private String icon;

    /**
     * 同级权限之间的排序号
     */
    private Integer sort;

    /**
     * 权限记录创建时间
     */
    private LocalDateTime createTime;

    /**
     * 权限记录最后更新时间
     */
    private LocalDateTime updateTime;
}

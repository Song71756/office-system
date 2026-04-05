package com.office.newofficeautomationbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;
/**
 * 用户实体类
 * 对应数据库中的用户表，存储用户的基本信息、权限状态及时间记录
 */
@Data
public class User {
    /**
     * 用户唯一标识 ID
     */
    private Integer id;

    /**
     * 用户名（登录账号）
     */
    private String username;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 用户真实姓名
     */
    private String realName;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 用户头像地址
     */
    private String avatar;

    /**
     * 所属部门 ID (包装类型，允许为 null)
     */
    private Integer departmentId;

    /**
     * 用户状态（0:禁用, 1:启用）
     */
    private Integer status;

    /**
     * 账号创建时间
     */
    private LocalDateTime createTime;

    /**
     * 账号信息更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 最后一次登录时间
     */
    private LocalDateTime lastLoginTime;

}

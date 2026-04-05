package com.office.newofficeautomationbackend.common.annotation;

/**
 * 权限校验的逻辑关系枚举
 * 用于在 @CheckPermission 注解中指定多个权限编码之间的匹配规则
 */
public enum Logical {
    /**
     * 且关系：用户必须同时拥有注解中指定的所有权限编码，才能访问该接口
     */
    AND,
    /**
     * 或关系：用户只要拥有注解中指定权限编码中的任意一个，即可访问该接口
     */
    OR
}

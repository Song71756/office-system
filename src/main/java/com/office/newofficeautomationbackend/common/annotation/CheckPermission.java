package com.office.newofficeautomationbackend.common.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 标注在 Controller 的方法上，用于指定访问该接口所需的权限编码
 * 例如：@CheckPermission({"user:add", "user:update"})
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckPermission {
    /**
     * 权限编码值数组
     */
    String[] value();

    /**
     * 校验逻辑：默认必须同时拥有 (AND)
     */
    Logical logical() default Logical.AND;
}

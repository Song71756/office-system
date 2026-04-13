package com.office.newofficeautomationbackend.common;

/**
 * 自定义业务异常
 * 用于在业务逻辑中抛出可预期的错误，由全局异常处理器统一捕获并返回友好提示
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}

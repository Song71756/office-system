package com.office.newofficeautomationbackend.common;

import lombok.Getter;

@Getter
public enum ResultCode {
    
    SUCCESS(200, "成功"),
    ERROR(500, "服务器错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    BAD_REQUEST(400, "请求参数错误"),
    VALIDATION_ERROR(422, "数据验证失败");
    
    private final int code;
    private final String message;
    
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
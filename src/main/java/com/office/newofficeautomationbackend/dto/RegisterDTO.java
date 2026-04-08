package com.office.newofficeautomationbackend.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 注册请求参数校验 DTO
 */
@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^.{2,10}$", message = "用户名长度必须在2-10之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^.{6,20}$", message = "密码长度必须在6-20之间")
    private String password;

    @NotBlank(message = "用户真实名称不能为空")
    @Pattern(regexp = "^.{2,10}$", message = "真实名称长度必须在2-10之间")
    private String realName;

    @Pattern(regexp = "^$|^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}

package com.office.newofficeautomationbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 登录请求参数校验 DTO
 */
@Data
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^.{2,10}$", message = "用户名长度必须在2-10之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^.{6,20}$", message = "密码长度必须在6-20之间")
    private String password;
}

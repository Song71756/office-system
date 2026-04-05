package com.office.newofficeautomationbackend.dto;

import com.office.newofficeautomationbackend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录成功后的响应数据传输对象
 * 包含 Token 以及前端展示所需的当前用户信息和权限列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    /**
     * JWT 访问令牌
     */
    private String token;

    /**
     * 当前登录用户的基本资料 (已脱敏)
     */
    private UserDTO userDTO;

    /**
     * 该用户拥有的所有权限编码列表 (如 ["user:add", "file:upload"])
     * 前端根据此列表控制按钮的显示/隐藏
     */
    private List<String> permissions;
}

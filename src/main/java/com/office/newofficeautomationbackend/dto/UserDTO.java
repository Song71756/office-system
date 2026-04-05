package com.office.newofficeautomationbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class UserDTO {


        private Integer id;
        private String username;
        private String password;
        private String realName;
        private String email;
        private String phone;
        private String avatar;
        private Integer departmentId;
        private String departmentName;  // 新增字段：部门名称
        private String roleName;  // 新增字段：角色名称
        private Integer status;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private LocalDateTime lastLoginTime;

}

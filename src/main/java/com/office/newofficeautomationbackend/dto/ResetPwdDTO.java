package com.office.newofficeautomationbackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResetPwdDTO {
    @NotNull(message= "用户id不能为空")
    private Integer id;

    private String newPassword;
}

package com.office.newofficeautomationbackend.controller;

import com.office.newofficeautomationbackend.common.Result;
import com.office.newofficeautomationbackend.entity.Permission;
import com.office.newofficeautomationbackend.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制层
 */
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * 获取所有权限列表 (用于构建树形结构)
     */
    @GetMapping("/list")
    public Result<List<Permission>> list() {
        return Result.success(permissionService.list());
    }

    /**
     * 新增或更新权限
     */
    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody Permission permission) {
        return Result.success(permissionService.saveOrUpdate(permission));
    }

    /**
     * 根据 ID 删除权限
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        return Result.success(permissionService.deleteById(id));
    }
}

package com.office.newofficeautomationbackend.controller;

import com.office.newofficeautomationbackend.common.Result;
import com.office.newofficeautomationbackend.entity.Permission;
import com.office.newofficeautomationbackend.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
     * 根据角色 ID 获取该角色拥有的权限列表（权限编码 -> 权限名称）
     */
    @GetMapping("/role/{roleId}")
    public Result<Map<String, String>> getPermissionsByRoleId(@PathVariable Integer roleId) {
        return Result.success(permissionService.getPermissionMapByRoleId(roleId));
    }

    /**
     * 获取所有角色对应的权限列表（角色名称 -> 权限编码与权限名称的映射）
     */
    @GetMapping("/roles/all")
    public Result<Map<String, Map<String, String>>> getAllRolePermissions() {
        return Result.success(permissionService.getAllRolePermissionMap());
    }

    /**
     * 根据 ID 删除权限
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Integer id) {
        return Result.success(permissionService.deleteById(id));
    }
}

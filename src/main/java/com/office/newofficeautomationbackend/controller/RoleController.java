package com.office.newofficeautomationbackend.controller;

import com.office.newofficeautomationbackend.common.Result;
import com.office.newofficeautomationbackend.common.annotation.CheckPermission;
import com.office.newofficeautomationbackend.entity.Role;
import com.office.newofficeautomationbackend.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制层
 */
@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 获取所有角色列表
     */
    @GetMapping("/list")
    @CheckPermission("api:role:list")
    public Result<List<Role>> list() {
        return Result.success(roleService.list());
    }

    /**
     * 根据 ID 获取角色详情
     */
    @GetMapping("/{id}")
    @CheckPermission("role:view")
    public Result<Role> getById(@PathVariable Integer id) {
        return Result.success(roleService.getById(id));
    }

    /**
     * 新增或更新角色
     */
    @PostMapping("/save")
    @CheckPermission({"role:add","role:edit"})
    public Result<Boolean> save(@RequestBody Role role) {
        return Result.success(roleService.saveOrUpdate(role));
    }

    /**
     * 为角色分配权限
     * @param roleId 角色 ID
     * @param permissionIds 选中的权限 ID 列表 (通过 JSON 数组传递)
     */
    @PostMapping("/assignPermissions/{roleId}")
    @CheckPermission("role:assign")
    public Result<Boolean> assignPermissions(@PathVariable Integer roleId, @RequestBody List<Integer> permissionIds) {
        return Result.success("分配成功", roleService.assignPermissions(roleId, permissionIds));
    }

    /**
     * 根据 ID 删除角色
     */
    @DeleteMapping("/delete/{id}")
    @CheckPermission("role:delete")
    public Result<Boolean> delete(@PathVariable Integer id) {
        return Result.success(roleService.deleteById(id));
    }
}

package com.office.newofficeautomationbackend.controller;

import com.office.newofficeautomationbackend.common.Result;
import com.office.newofficeautomationbackend.common.annotation.CheckPermission;
import com.office.newofficeautomationbackend.entity.Department;
import com.office.newofficeautomationbackend.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制层
 * 提供组织架构的树形展示及 CRUD 维护接口
 */
@RestController
@RequestMapping("/department")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    /**
     * 获取全量部门树形结构
     * 适用于前端组织架构树、下拉选择框等场景
     * @return 包含层级关系的部门列表
     */
    @GetMapping("/tree")
    @CheckPermission("dept:list")
    public Result<List<Department>> getTree() {
        return Result.success("获取部门树成功", departmentService.getTree());
    }

    /**
     * 获取扁平化的部门全列表
     */
    @GetMapping("/list")
    @CheckPermission("dept:list")
    public Result<List<Department>> list() {
        return Result.success(departmentService.list());
    }

    /**
     * 根据 ID 获取部门详情
     */
    @GetMapping("/{id}")
    @CheckPermission("dept:list")
    public Result<Department> getById(@PathVariable Integer id) {
        return Result.success(departmentService.getById(id));
    }

    /**
     * 保存或更新部门信息
     * @param department 部门 JSON 数据
     */
    @PostMapping("/save")
    @CheckPermission("dept:edit")
    public Result<Boolean> save(@RequestBody Department department) {
        return Result.success("保存成功", departmentService.saveOrUpdate(department));
    }

    /**
     * 删除部门
     * 逻辑说明：若存在下级部门或关联员工则会抛出业务异常
     * @param id 部门 ID
     */
    @DeleteMapping("/{id}")
    @CheckPermission("dept:delete")
    public Result<Boolean> delete(@PathVariable Integer id) {
        return Result.success("删除成功", departmentService.deleteById(id));
    }
}

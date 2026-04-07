package com.office.newofficeautomationbackend.service.impl;

import com.office.newofficeautomationbackend.entity.Department;
import com.office.newofficeautomationbackend.mapper.DepartmentMapper;
import com.office.newofficeautomationbackend.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门管理业务层实现类
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public List<Department> list() {
        return departmentMapper.list();
    }

    /**
     * 实现：构建部门树形结构
     * 1. 查询出所有部门数据
     * 2. 筛选出顶层部门 (parentId 为 0 或 null)
     * 3. 递归寻找每个顶层部门的子部门
     */
    @Override
    public List<Department> getTree() {
        // 1. 获取所有部门
        List<Department> allDepartments = departmentMapper.list();
        
        // 2. 找到所有根部门 (约定 parentId 为 0 或 null 为根)
        return allDepartments.stream()
                .filter(dept -> dept.getParentId() == null || dept.getParentId() == 0)
                .map(dept -> {
                    dept.setChildren(getChildren(dept, allDepartments));
                    return dept;
                })
                .collect(Collectors.toList());
    }

    /**
     * 递归辅助方法：获取某个部门下的所有子部门列表
     * @param root 当前父部门
     * @param all 所有部门数据源
     * @return 挂载好子部门的列表
     */
    private List<Department> getChildren(Department root, List<Department> all) {
        return all.stream()
                .filter(dept -> root.getId().equals(dept.getParentId()))
                .map(dept -> {
                    dept.setChildren(getChildren(dept, all));
                    return dept;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Department getById(Integer id) {
        return departmentMapper.getById(id);
    }

    @Override
    public boolean saveOrUpdate(Department department) {
        if (department.getId() == null) {
            if(department.getParentId() ==0){
                department.setParentId(null);
            }
            // 新增逻辑：初始化时间戳
            department.setCreateTime(LocalDateTime.now());
            department.setUpdateTime(LocalDateTime.now());
            return departmentMapper.insert(department) > 0;
        } else {
            if(department.getParentId() ==0){
                department.setParentId(null);
            }
            // 更新逻辑：维护更新时间
            department.setUpdateTime(LocalDateTime.now());
            return departmentMapper.update(department) > 0;
        }
    }

    /**
     * 实现：删除部门 (带业务校验)
     * 规则：
     * 1. 若该部门下存在子部门，不允许删除
     * 2. 若该部门下已分配员工，不允许删除
     */
    @Override
    public boolean deleteById(Integer id) {
        // 1. 校验子部门
        if (departmentMapper.countChildren(id) > 0) {
            throw new RuntimeException("该部门下存在子部门，请先删除或迁移子部门。");
        }
        
        // 2. 校验关联用户
        if (departmentMapper.countUsers(id) > 0) {
            throw new RuntimeException("该部门下仍有员工在职，无法直接删除。");
        }
        
        return departmentMapper.deleteById(id) > 0;
    }
}

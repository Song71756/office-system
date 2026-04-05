package com.office.newofficeautomationbackend.service.impl;

import com.office.newofficeautomationbackend.entity.Role;
import com.office.newofficeautomationbackend.mapper.RoleMapper;
import com.office.newofficeautomationbackend.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色管理业务逻辑实现类
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    /**
     * 实现：获取角色全列表
     * 按创建时间降序排列返回
     */
    @Override
    public List<Role> list() {
        return roleMapper.list();
    }

    /**
     * 实现：查询角色详情
     */
    @Override
    public Role getById(Integer id) {
        return roleMapper.getById(id);
    }

    /**
     * 实现：保存或更新角色
     * 根据是否包含 ID 自动判断是执行数据库 INSERT 还是 UPDATE，并自动维护时间戳
     */
    @Override
    public boolean saveOrUpdate(Role role) {
        if (role.getId() == null) {
            // 新增：初始化创建时间与更新时间
            role.setCreateTime(LocalDateTime.now());
            role.setUpdateTime(LocalDateTime.now());
            return roleMapper.insert(role) > 0;
        } else {
            // 更新：仅修改更新时间
            role.setUpdateTime(LocalDateTime.now());
            return roleMapper.update(role) > 0;
        }
    }

    /**
     * 实现：物理删除角色记录
     */
    @Override
    public boolean deleteById(Integer id) {
        return roleMapper.deleteById(id) > 0;
    }

    /**
     * 实现：获取用户的角色集合
     */
    @Override
    public List<Role> getRolesByUserId(Integer userId) {
        return roleMapper.selectRolesByUserId(userId);
    }

    /**
     * 实现：分配权限逻辑
     * 采用“先清空，后批量插入”的稳健方案，并开启事务保证一致性
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissions(Integer roleId, List<Integer> permissionIds) {
        // 1. 首先清空该角色原有的所有权限关联
        roleMapper.deleteRolePermissions(roleId);

        // 2. 如果新的权限列表不为空，则执行批量插入
        if (permissionIds != null && !permissionIds.isEmpty()) {
            roleMapper.insertRolePermissions(roleId, permissionIds);
        }
        
        return true;
    }
}

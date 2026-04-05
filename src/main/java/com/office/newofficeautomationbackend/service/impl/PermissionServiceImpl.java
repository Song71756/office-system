package com.office.newofficeautomationbackend.service.impl;

import com.office.newofficeautomationbackend.entity.Permission;
import com.office.newofficeautomationbackend.mapper.PermissionMapper;
import com.office.newofficeautomationbackend.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限管理业务逻辑实现类
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    /**
     * 实现：获取所有权限记录
     * 返回所有已配置的菜单、目录、按钮或 API 权限项
     */
    @Override
    public List<Permission> list() {
        return permissionMapper.list();
    }

    /**
     * 实现：根据 ID 获取权限详情
     */
    @Override
    public Permission getById(Integer id) {
        return permissionMapper.getById(id);
    }

    /**
     * 实现：保存或更新权限
     * 根据是否包含 ID 自动判断是执行数据库 INSERT 还是 UPDATE，并自动维护时间戳
     */
    @Override
    public boolean saveOrUpdate(Permission permission) {
        if (permission.getId() == null) {
            // 新增：设置初始时间戳
            permission.setCreateTime(LocalDateTime.now());
            permission.setUpdateTime(LocalDateTime.now());
            return permissionMapper.insert(permission) > 0;
        } else {
            // 更新：维护更新时间戳
            permission.setUpdateTime(LocalDateTime.now());
            return permissionMapper.update(permission) > 0;
        }
    }

    /**
     * 实现：删除指定权限
     */
    @Override
    public boolean deleteById(Integer id) {
        return permissionMapper.deleteById(id) > 0;
    }

    /**
     * 实现：根据用户 ID 获取全量权限
     * 该逻辑涉及 sys_user_role 和 sys_role_permission 两张关联表的多表联查
     */
    @Override
    public List<Permission> getPermissionsByUserId(Integer userId) {
        return permissionMapper.selectPermissionsByUserId(userId);
    }
}

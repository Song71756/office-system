package com.office.newofficeautomationbackend.service;

import com.office.newofficeautomationbackend.entity.Permission;
import java.util.List;

/**
 * 权限管理业务逻辑接口
 * 定义了对系统权限项（菜单、按钮、接口等）进行管理的核心业务方法
 */
public interface PermissionService {
    /**
     * 获取系统中所有已定义的权限记录列表
     * @return 包含所有权限的 List 集合
     */
    List<Permission> list();

    /**
     * 根据主键 ID 获取权限详细信息
     * @param id 权限 ID
     * @return 匹配的权限实体对象
     */
    Permission getById(Integer id);

    /**
     * 保存或更新权限信息
     * 如果 permission 包含 ID，则执行更新操作；否则执行新增操作
     * @param permission 权限实体数据
     * @return 操作是否成功 (true 为成功)
     */
    boolean saveOrUpdate(Permission permission);

    /**
     * 根据主键 ID 物理删除权限项
     * @param id 待删除的权限 ID
     * @return 操作是否成功 (true 为成功)
     */
    boolean deleteById(Integer id);

    /**
     * 根据用户 ID 穿透查询该用户所拥有的所有权限编码列表
     * 该方法实现了用户-角色-权限的多表关联提取
     * @param userId 用户唯一 ID
     * @return 该用户拥有的所有权限对象集合
     */
    List<Permission> getPermissionsByUserId(Integer userId);

    /**
     * 统计某权限下有多少子权限
     * @param id 父权限 ID
     * @return 子权限数量
     */
    int countChildren(Integer id);
}

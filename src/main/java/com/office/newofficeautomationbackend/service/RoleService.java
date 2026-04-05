package com.office.newofficeautomationbackend.service;

import com.office.newofficeautomationbackend.entity.Role;
import java.util.List;

/**
 * 角色管理业务逻辑接口
 * 定义了对系统角色进行管理的核心业务方法，包括 CRUD 以及权限分配功能
 */
public interface RoleService {
    /**
     * 获取系统中所有已定义的角色列表
     * @return 包含所有角色的 List 集合
     */
    List<Role> list();

    /**
     * 根据主键 ID 获取详细的角色信息
     * @param id 角色唯一 ID
     * @return 匹配的角色实体对象，若不存在则返回 null
     */
    Role getById(Integer id);

    /**
     * 保存或更新角色信息
     * 如果 role 对象中包含 ID，则执行更新操作；否则执行新增操作
     * @param role 角色数据实体
     * @return 操作是否成功 (true 为成功)
     */
    boolean saveOrUpdate(Role role);

    /**
     * 根据主键 ID 物理删除指定的角色记录
     * @param id 待删除的角色 ID
     * @return 操作是否成功 (true 为成功)
     */
    boolean deleteById(Integer id);

    /**
     * 获取指定用户所拥有的所有角色记录
     * 涉及 sys_user_role 关联表的查询逻辑
     * @param userId 用户唯一 ID
     * @return 该用户拥有的角色集合
     */
    List<Role> getRolesByUserId(Integer userId);

    /**
     * 为指定角色批量重新分配权限
     * 该操作会清空角色现有的所有权限关联，并根据传入的 ID 列表建立新的关联
     * @param roleId 角色 ID
     * @param permissionIds 选中的权限 ID 集合，若传空列表则表示取消所有权限
     * @return 分配操作是否成功
     */
    boolean assignPermissions(Integer roleId, List<Integer> permissionIds);
}

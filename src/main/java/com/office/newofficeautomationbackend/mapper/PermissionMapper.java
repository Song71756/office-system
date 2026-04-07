package com.office.newofficeautomationbackend.mapper;

import com.office.newofficeautomationbackend.entity.Permission;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 权限管理持久层接口 (MyBatis Mapper)
 * 负责权限表的 CRUD 操作，以及处理角色与权限的多对多映射查询
 */
@Mapper
public interface PermissionMapper {

    /**
     * 根据主键查询权限详情
     */
    @Select("SELECT * FROM sys_permission WHERE id = #{id}")
    Permission getById(Integer id);

    /**
     * 查询所有权限列表 (支持树形展示的基础数据)
     */
    @Select("SELECT * FROM sys_permission ORDER BY sort ASC")
    List<Permission> list();

    /**
     * 新增权限项
     */
    @Insert("INSERT INTO sys_permission (permission_code, permission_name, type, parent_id, path, icon, sort, create_time, update_time) " +
            "VALUES (#{permissionCode}, #{permissionName}, #{type}, #{parentId}, #{path}, #{icon}, #{sort}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Permission permission);

    /**
     * 更新权限信息
     */
    @Update("UPDATE sys_permission SET permission_code=#{permissionCode}, permission_name=#{permissionName}, type=#{type}, " +
            "parent_id=#{parentId}, path=#{path}, icon=#{icon}, sort=#{sort}, update_time=#{updateTime} WHERE id=#{id}")
    int update(Permission permission);

    /**
     * 根据 ID 删除权限
     */
    @Delete("DELETE FROM sys_permission WHERE id = #{id}")
    int deleteById(Integer id);

    /**
     * 【核心】根据角色 ID 查询该角色拥有的所有权限
     * 涉及关联表：sys_role_permission
     */
    @Select("SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId}")
    List<Permission> selectPermissionsByRoleId(Integer roleId);

    /**
     * 【高级核心】根据用户 ID 直接查询该用户拥有的所有权限
     * 逻辑：用户 -> 角色 -> 权限 (穿透两层关联表)
     * 用于登录成功后快速加载用户的权限编码列表
     */
    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<Permission> selectPermissionsByUserId(Integer userId);

    /**
     * 统计某权限下有多少子权限
     * @param id 父权限 ID
     * @return 子权限数量
     */
    @Select("SELECT COUNT(*) FROM sys_permission WHERE parent_id = #{id}")
    int countChildren(Integer id);
}

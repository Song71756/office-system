package com.office.newofficeautomationbackend.mapper;

import com.office.newofficeautomationbackend.entity.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 角色管理持久层接口 (MyBatis Mapper)
 * 负责角色表的 CRUD 操作，以及处理用户与角色的多对多映射查询
 */
@Mapper
public interface RoleMapper {

    /**
     * 根据主键查询角色详情
     */
    @Select("SELECT * FROM sys_role WHERE id = #{id}")
    Role getById(Integer id);

    /**
     * 查询所有角色列表
     */
    @Select("SELECT * FROM sys_role ORDER BY create_time DESC")
    List<Role> list();

    /**
     * 新增角色记录
     */
    @Insert("INSERT INTO sys_role (role_code, role_name, description, create_time, update_time) " +
            "VALUES (#{roleCode}, #{roleName}, #{description}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Role role);

    /**
     * 更新角色信息
     */
    @Update("UPDATE sys_role SET role_code=#{roleCode}, role_name=#{roleName}, description=#{description}, update_time=#{updateTime} WHERE id=#{id}")
    int update(Role role);

    /**
     * 根据 ID 删除角色
     */
    @Delete("DELETE FROM sys_role WHERE id = #{id}")
    int deleteById(Integer id);

    /**
     * 【核心】根据用户 ID 查询该用户拥有的所有角色
     * 涉及关联表：sys_user_role
     * @param userId 用户唯一 ID
     * @return 该用户拥有的角色集合
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<Role> selectRolesByUserId(Integer userId);

    /**
     * 清空某个角色的所有权限关联
     * @param roleId 角色 ID
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    void deleteRolePermissions(Integer roleId);

    /**
     * 批量为角色插入权限关联
     * @param roleId 角色 ID
     * @param permissionIds 权限 ID 列表
     */
    @Insert("<script>" +
            "INSERT INTO sys_role_permission (role_id, permission_id) VALUES " +
            "<foreach collection='permissionIds' item='id' separator=','>" +
            "(#{roleId}, #{id})" +
            "</foreach>" +
            "</script>")
    void insertRolePermissions(@Param("roleId") Integer roleId, @Param("permissionIds") List<Integer> permissionIds);
}

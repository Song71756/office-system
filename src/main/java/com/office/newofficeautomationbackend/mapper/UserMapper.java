package com.office.newofficeautomationbackend.mapper;

import com.office.newofficeautomationbackend.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户管理持久层接口 (MyBatis Mapper)
 * 负责与数据库中的用户表进行直接交互，实现核心的 SQL 增删改查逻辑
 */
@Mapper
public interface UserMapper {

    /**
     * 查询所有用户信息
     * 自动映射说明：数据库字段 (real_name, phone, email...) 将自动转换为 Java 属性 (realName, phone, email...)
     * @return 包含所有用户的 List 集合
     */
    @Select("SELECT * FROM sys_user")
    List<User> list();

    /**
     * 根据主键 ID 获取单个用户的详细信息
     * @param id 用户唯一标识 ID (主键)
     * @return 查询到的 User 对象，若不存在则返回 null
     */
    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    User getById(Integer id);



    /**
     * 根据用户id查询用户角色id
     * @param id 用户id
     * @return 用户角色id
     */
    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{id}")
    Integer getRoleIdByUserId(Integer id);




    /**
     * 根据用户名获取用户信息
     * 作用：用于用户注册时的唯一性校验
     * @param username 待查询的用户名
     * @return 匹配的用户信息对象，若不存在则返回 null
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User getByUsername(String username);

    /**
     * 持久化新增用户信息到数据库
     * 注意：使用了 @Options 获取自动生成的自增主键 (id) 并回填到 User 对象中
     * 字段映射：username, password, real_name (对应 realName), email, phone, avatar, department_id (对应 departmentId), status, create_time, update_time
     * @param user 待插入的用户实体
     * @return 受影响的行数 (1 表示插入成功)
     */
    @Insert("INSERT INTO sys_user (username, password, real_name, email, phone, avatar, department_id, status, create_time, update_time) " +
            "VALUES (#{username}, #{password}, #{realName}, #{email}, #{phone}, #{avatar}, #{departmentId}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    /**
     * 更新现有用户的资料信息
     * 仅支持更新基础属性字段：username, real_name, email, phone, status, update_time
     * @param user 包含更新信息的用户实体 (必须包含有效的 id)
     * @return 受影响的行数 (1 表示更新成功)
     */
    @Update("UPDATE sys_user SET username=#{username}, real_name=#{realName}, email=#{email}, phone=#{phone}, avatar=#{avatar}, status=#{status}, update_time=#{updateTime} WHERE id=#{id}")
    int update(User user);

    /**
     * 仅更新用户头像路径
     * @param id 用户 ID
     * @param avatar 新头像路径
     * @return 受影响的行数
     */
    @Update("UPDATE sys_user SET avatar=#{avatar}, update_time=NOW() WHERE id=#{id}")
    int updateAvatar(@Param("id") Integer id, @Param("avatar") String avatar);

    /**
     * 根据关键词模糊查询用户列表
     * 支持匹配：用户名 (username) 或 真实姓名 (realName)
     * @param keyword 搜索关键词
     * @return 匹配的用户列表 (PageHelper 将拦截此结果并处理分页)
     */
    @Select("SELECT * FROM sys_user WHERE username LIKE CONCAT('%', #{keyword}, '%') OR real_name LIKE CONCAT('%', #{keyword}, '%')")
    List<User> selectByKeyword(@Param("keyword") String keyword);

    /**
     * 更新用户的登录密码
     * @param id 用户主键 ID
     * @param newPassword 已经过 BCrypt 加密的新密码密文
     * @return 受影响的行数 (1 表示更新成功)
     */
    @Update("UPDATE sys_user SET password = #{newPassword}, update_time = NOW() WHERE id = #{id}")
    int updatePassword(@Param("id") Integer id, @Param("newPassword") String newPassword);

    /**
     * 更新用户的最后一次登录时间
     * @param id 用户主键 ID
     * @param lastLoginTime 登录发生的时间点
     * @return 受影响的行数
     */
    @Update("UPDATE sys_user SET last_login_time = #{lastLoginTime} WHERE id = #{id}")
    int updateLastLoginTime(@Param("id") Integer id, @Param("lastLoginTime") java.time.LocalDateTime lastLoginTime);

    /**
     * 根据 ID 执行用户数据的物理删除
     * ⚠️ 请谨慎操作：此方法会直接从数据库表中移除该记录
     * @param id 待删除用户的 ID
     * @return 受影响的行数 (1 表示删除成功)
     */
    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int deleteById(Integer id);

    /**
     * 清空某个用户的所有角色关联
     * @param userId 用户 ID
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    void deleteUserRoles(Integer userId);

    /**
     * 批量为用户插入角色关联
     * @param userId 用户 ID
     * @param roleIds 角色 ID 列表
     */
    @Insert("<script>" +
            "INSERT INTO sys_user_role (user_id, role_id) VALUES " +
            "<foreach collection='roleIds' item='id' separator=','>" +
            "(#{userId}, #{id})" +
            "</foreach>" +
            "</script>")
    void insertUserRoles(@Param("userId") Integer userId, @Param("roleIds") List<Integer> roleIds);
}

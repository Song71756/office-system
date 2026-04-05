package com.office.newofficeautomationbackend.service;

import com.office.newofficeautomationbackend.dto.LoginResponseDTO;
import com.office.newofficeautomationbackend.dto.UserDTO;
import com.office.newofficeautomationbackend.entity.User;
import com.github.pagehelper.PageInfo;
import java.util.List;

/**
 * 用户业务逻辑接口
 * 定义了对用户数据进行管理的核心业务方法
 */
public interface UserService {
    /**
     * 获取用户全量列表
     * @return 用户集合
     */
    List<User> list();

    /**
     * 分页模糊查询用户列表
     * @param pageNum 当前页码
     * @param pageSize 每页显示条数
     * @param keyword 搜索关键词 (用户名或姓名)
     * @return 包含分页详细信息的 PageInfo 对象
     */
    PageInfo<UserDTO> findPage(int pageNum, int pageSize, String keyword);

    /**
     * 根据主键 ID 获取详细用户信息
     * @param id 用户唯一 ID
     * @return 匹配的用户信息对象
     */
    UserDTO getById(Integer id);

    /**
     * 根据用户名获取用户信息
     * @param username 用户名
     * @return 匹配的用户信息对象
     */
    UserDTO getByUsername(String username);

    /**
     * 持久化新增用户信息
     * @param user 用户实体数据
     * @return 是否保存成功 (true 为成功)
     */
    boolean save(User user);

    /**
     * 用户注册业务逻辑
     * 包含：用户名冲突校验、密码 BCrypt 加密、默认状态及时间填充
     * @param user 前端提交的待注册用户数据
     * @return 注册成功的用户对象 (回填了 ID 和加密后的密码)
     */
    User register(User user);

    /**
     * 用户登录业务逻辑 (增强版)
     * 1. 校验用户名是否存在
     * 2. 匹配 BCrypt 密码是否正确
     * 3. 登录成功则生成 JWT Token
     * 4. 加载用户基本资料及拥有的权限列表
     * @param username 用户名
     * @param password 明文密码
     * @return 包含 Token、用户信息和权限列表的 DTO 对象
     */
    LoginResponseDTO login(String username, String password);

    /**
     * 修改用户登录密码
     * @param username 当前登录的用户名 (从 Token 解析得到)
     * @param oldPassword 旧密码 (明文)
     * @param newPassword 新密码 (明文)
     * @return 修改是否成功
     */
    boolean updatePassword(String username, String oldPassword, String newPassword);

    /**
     * 更新现有用户信息
     * @param user 需要更新的用户对象 (需包含 ID)
     * @return 是否更新成功 (true 为成功)
     */
    boolean update(User user);

    /**
     * 执行用户数据物理删除
     * @param id 用户主键 ID
     * @return 是否成功移除 (true 为成功)
     */
    boolean deleteById(Integer id);

    /**
     * 为指定用户分配角色
     * @param userId 用户 ID
     * @param roleIds 角色 ID 集合
     * @return 是否分配成功
     */
    boolean assignRoles(Integer userId, List<Integer> roleIds);

    /**
     * 仅更新用户头像路径
     * @param id 用户 ID
     * @param avatar 新头像路径
     * @return 是否更新成功
     */
    boolean updateAvatar(Integer id, String avatar);
}

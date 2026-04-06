package com.office.newofficeautomationbackend.service.impl;

import com.github.pagehelper.Page;
import com.office.newofficeautomationbackend.dto.LoginResponseDTO;
import com.office.newofficeautomationbackend.dto.UserDTO;
import com.office.newofficeautomationbackend.entity.Permission;
import com.office.newofficeautomationbackend.service.DepartmentService;
import com.office.newofficeautomationbackend.service.RoleService;
import com.office.newofficeautomationbackend.utils.JwtUtils;
import com.office.newofficeautomationbackend.entity.User;
import com.office.newofficeautomationbackend.mapper.UserMapper;
import com.office.newofficeautomationbackend.service.PermissionService;
import com.office.newofficeautomationbackend.service.UserService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户管理业务逻辑层实现
 * 负责组装 Mapper 接口提供的基础持久化能力，为 Controller 提供面向业务的 User 数据管理服务
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private RoleService roleService;

    // 引入密码加密器 (由 Spring Security 提供)
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /** Redis Key 前缀：用户权限缓存 */
    private static final String PERMISSION_CACHE_PREFIX = "user:permissions:";
    /** 权限缓存过期时间（小时），与 JWT Token 有效期保持一致 */
    private static final long PERMISSION_CACHE_HOURS = 24;

    /**
     * 实现：获取用户全量列表
     * 调用持久层全查 SQL，获取数据库中存储的所有用户记录
     */
    @Override
    public List<User> list() {

        return userMapper.list();
    }

    /**
     * 实现：分页模糊查询用户列表
     * 1. 使用 PageHelper.startPage 设置分页参数
     * 2. 执行 Mapper 查询方法
     * 3. 封装 PageInfo 并返回
     */
    @Override
    public PageInfo<UserDTO> findPage(int pageNum, int pageSize, String keyword) {
        // 设置分页参数 (PageHelper 会在执行 SQL 前自动拼接 LIMIT 语句)
        PageHelper.startPage(pageNum, pageSize);
        
        // 执行查询逻辑 (支持模糊搜索)
        List<UserDTO> userDTOList = userMapper.selectUserDTOByKeyword(keyword);
        
        // 安全处理：批量将返回的密码脱敏
        userDTOList.forEach(u -> u.setPassword(null));

//        // 使用Page对象获取分页信息
//        Page<User> page = (Page<User>) userList;
//
//        List<UserDTO> userDTOList = new ArrayList<>();
//
//        for(User user : userList){
//            UserDTO userDTO = this.toUserDTO(user);
//            userDTOList.add(userDTO);
//        }
//
//        // 创建新的PageInfo，保留原始的分页信息
//        PageInfo<UserDTO> pageInfo = new PageInfo<>(userDTOList);
//        pageInfo.setPageNum(page.getPageNum());
//        pageInfo.setPageSize(page.getPageSize());
//        pageInfo.setTotal(page.getTotal());
//        pageInfo.setPages(page.getPages());
        
        return new PageInfo<>(userDTOList);
    }

    /**
     * 实现：获取单个用户详情信息
     * 基于唯一主键 ID 进行查询，返回包含用户详细属性的对象
     */
    @Override
    public UserDTO getById(Integer id) {
        User user = userMapper.getById(id);
        // 脱敏处理并封装返回
        if(user != null){
            user.setPassword(null);
        }

        //转换成UserDTO
        UserDTO userDTO = this.toUserDTO(user);

        return userDTO;
    }

    /**
     * 实现：根据用户名获取详情
     * 用于校验用户是否存在
     */
    @Override
    public UserDTO getByUsername(String username) {
        User user = userMapper.getByUsername(username);
        // 脱敏处理并封装返回
        if(user != null){
            user.setPassword(null);
        }

        //转换成UserDTO
        UserDTO userDTO = this.toUserDTO(user);

        return userDTO;
    }

    /**
     * 实现：持久化存储一个新用户
     * 该操作会将实体类字段映射到数据库，并将数据库生成的自增 ID 回填到 user 对象中
     */
    @Override
    public boolean save(User user) {
        return userMapper.insert(user) > 0;
    }

    /**
     * 实现：用户注册逻辑
     * 1. 唯一性检查：若用户名已存在，抛出异常或返回 null (此处返回 null 示意失败)
     * 2. 密码加密：将原始密码转换为加密后的哈希字符串
     * 3. 字段补全：设置状态为 1 (启用)，记录创建和更新时间
     */
    @Override
    public User register(User user) {
        // 1. 校验用户名是否冲突
        if (userMapper.getByUsername(user.getUsername()) != null) {
            throw new RuntimeException("该用户名已被占用，请更换。");
        }

        // 2. 密码 BCrypt 加密处理
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // 3. 自动补全基础字段
        // 如果前端没有传 status，则默认设置为 1 (启用)
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 4. 执行插入操作
        int rows = userMapper.insert(user);
        
        return rows > 0 ? user : null;
    }

    /**
     * 实现：用户登录逻辑 (增强版)
     * 1. 根据用户名查找用户
     * 2. 校验用户是否存在及其状态是否正常
     * 3. 校验密码是否匹配
     * 4. 更新最后登录时间
     * 5. 生成 JWT Token
     * 6. 加载权限列表并封装 DTO 返回
     */
    @Override
    public LoginResponseDTO login(String username, String password) {
        // 1. 获取用户信息
        User user = userMapper.getByUsername(username);
        
        // 2. 校验
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("该账号已被禁用");
        }
        
        // 3. 校验密码 (BCrypt 匹配)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 4. 更新最后登录时间
        userMapper.updateLastLoginTime(user.getId(), LocalDateTime.now());
        
        // 5. 生成 Token
        String token = jwtUtils.generateToken(username);

        // 6. 加载用户权限编码列表并缓存到 Redis
        List<Permission> permissionList = permissionService.getPermissionsByUserId(user.getId());
        List<String> permissionCodes = permissionList.stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toList());
        // 将权限列表写入 Redis，过期时间与 Token 一致
        redisTemplate.opsForValue().set(
                PERMISSION_CACHE_PREFIX + user.getId(),
                permissionCodes,
                PERMISSION_CACHE_HOURS, TimeUnit.HOURS);

        // 7. 脱敏处理并封装返回
        user.setPassword(null);
        // 8. 转换成UserDTO
        UserDTO userDTO = this.toUserDTO(user);


        return new LoginResponseDTO(token, userDTO, permissionCodes);
    }

    /**
     * 实现：修改密码业务逻辑
     * 1. 验证旧密码：使用 BCryptPasswordEncoder 校验当前登录用户的旧密码是否正确
     * 2. 校验新旧密码：新密码不应与旧密码相同 (可选逻辑)
     * 3. 加密新密码：对新密码进行 BCrypt 加密
     * 4. 执行更新
     */
    @Override
    public boolean updatePassword(String username, String oldPassword, String newPassword) {
        // 1. 获取当前用户
        User user = userMapper.getByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 验证旧密码 (明文 oldPassword 与 数据库密文 user.getPassword() 匹配)
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误，请核对后再试");
        }

        // 3. 校验新旧密码是否相同
        if (oldPassword.equals(newPassword)) {
            throw new RuntimeException("新密码不能与原密码相同");
        }

        // 4. 对新密码进行加密并更新
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        return userMapper.updatePassword(user.getId(), encodedNewPassword) > 0;
    }

    /**
     * 实现：修改现有的用户信息记录
     * 执行更新前需确保 user 对象中包含有效的 id
     */
    @Override
    public boolean update(User user) {
        User existingUser = userMapper.getById(user.getId());
        if (user.getStatus() == null) {
            user.setStatus(existingUser.getStatus());
        }
        return userMapper.update(user) > 0;
    }

    @Override
    public boolean updateAvatar(Integer id, String avatar) {
        return userMapper.updateAvatar(id, avatar) > 0;
    }

    /**
     * 实现：执行用户物理删除操作
     * 基于主键 ID 从数据库表中永久性移除该用户对应的记录
     */
    @Override
    public boolean deleteById(Integer id) {
        return userMapper.deleteById(id) > 0;
    }

    /**
     * 实现：为用户分配角色逻辑
     * 采用“先清空旧关联，后批量插入新关联”的策略
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoles(Integer userId, List<Integer> roleIds) {
        // 1. 先删除该用户现有的所有角色关联
        userMapper.deleteUserRoles(userId);

        // 2. 如果提供了新的角色 ID 列表，则执行批量插入
        if (roleIds != null && !roleIds.isEmpty()) {
            userMapper.insertUserRoles(userId, roleIds);
        }

        // 3. 清除该用户的权限缓存，下次请求时将从数据库重新加载
        redisTemplate.delete(PERMISSION_CACHE_PREFIX + userId);

        return true;
    }

    @Override
    public UserDTO toUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        // 加载部门名称
        userDTO.setDepartmentName(departmentService.getById(user.getDepartmentId()).getName());
        // 加载角色名称
        Integer roleId = userMapper.getRoleIdByUserId(user.getId());//根据用户id查询用户角色id
        userDTO.setRoleName(roleService.getById(roleId).getRoleName());//根据角色id查询角色名称


        return userDTO;
    }
}
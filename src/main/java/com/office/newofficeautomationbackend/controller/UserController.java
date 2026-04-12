package com.office.newofficeautomationbackend.controller;

import com.office.newofficeautomationbackend.common.Result;
import com.office.newofficeautomationbackend.common.annotation.CheckPermission;
import com.office.newofficeautomationbackend.common.annotation.Logical;
import com.office.newofficeautomationbackend.dto.LoginDTO;
import com.office.newofficeautomationbackend.dto.LoginResponseDTO;
import com.office.newofficeautomationbackend.dto.RegisterDTO;
import com.office.newofficeautomationbackend.dto.UserDTO;
import com.office.newofficeautomationbackend.entity.User;
import com.office.newofficeautomationbackend.config.LoginInterceptor;
import com.office.newofficeautomationbackend.service.UserService;
import com.office.newofficeautomationbackend.utils.JwtUtils;
import com.github.pagehelper.PageInfo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户管理 Web 控制层 (REST Controller)
 * 接收前端发起的 HTTP 请求，调用业务层服务并返回统一的 Result 包装结果
 */
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 分页查询用户信息 (支持根据用户名或姓名模糊搜索)
     * HTTP 方法：GET
     * API 路径：/user/page
     * @param pageNum 当前页码 (默认 1)
     * @param pageSize 每页条数 (默认 10)
     * @param keyword 搜索关键字 (可选)
     * @return 包含分页数据的 Result 对象
     */
    @GetMapping("/page")
    @CheckPermission("api:user:list") // 只有拥有 user:list 权限的用户才能访问分页列表
    public Result<PageInfo<UserDTO>> findPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "") String keyword) {
        PageInfo<UserDTO> pageData = userService.findPage(pageNum, pageSize, keyword);
        return Result.success("分页查询成功", pageData);
    }

    /**
     * 获取用户全量数据列表
     * HTTP 方法：GET
     * API 路径：/user/list
     * @return 包含用户集合的 Result 成功对象
     */
    @GetMapping("/list")
    @CheckPermission("api:user:list")
    public Result<List<User>> list() {
        return Result.success(userService.list());
    }

    /**
     * 获取当前登录用户的详细个人资料
     * HTTP 方法：GET
     * API 路径：/user/info
     * 注意：此方法必须定义在 getById(/{id}) 之前，防止路径变量匹配冲突
     * @param token 请求头中的 Authorization (由拦截器保证有效)
     * @return 包含用户详细信息 (已屏蔽密码) 的 Result 对象
     */
    @GetMapping("/info")
    public Result<UserDTO> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        // 1. 从 Token 中解析出用户名
        String username = jwtUtils.getUsernameFromToken(token);
        
        // 2. 根据用户名查询用户信息
        UserDTO userDTO = userService.getByUsername(username);
        
        return Result.success("获取个人信息成功", userDTO);
    }

    /**
     * 根据主键获取指定用户详情
     * HTTP 方法：GET
     * API 路径：/user/{id} (路径参数模式)
     * @param id 用户主键
     * @return 包含 User 实体详情的 Result 对象
     */
    @GetMapping("/{id}")
    @CheckPermission("user:view")
    public Result<UserDTO> getById(@PathVariable Integer id) {
        return Result.success(userService.getById(id));
    }

    /**
     * 用户注册接口
     * HTTP 方法：POST
     * API 路径：/user/register
     * 功能：处理新用户注册，包含密码 BCrypt 加密逻辑
     * @param registerDTO 前端传入的注册信息
     * @return 包含注册成功后用户信息的 Result 对象
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody @Valid RegisterDTO registerDTO) {
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(registerDTO.getPassword());
        user.setRealName(registerDTO.getRealName());
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        ;
        User registeredUser = userService.register(user);
        return Result.success("注册成功", registeredUser);
    }

    /**
     * 用户登录接口 (增强版)
     * HTTP 方法：POST
     * API 路径：/user/login
//     * @param user 包含用户名和密码的 JSON 对象
     * @return 包含生成的 JWT Token、用户信息及权限列表的 Result 对象
     */
    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@RequestBody @Valid LoginDTO loginDTO) {
        LoginResponseDTO loginResponse = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        return Result.success("登录成功", loginResponse);
    }

    /**
     * 修改用户密码
     * HTTP 方法：POST
     * API 路径：/user/updatePassword
     * @param params 包含 oldPassword 和 newPassword 的 Map
     * @param token 请求头中的 Authorization (由拦截器保证有效)
     * @return 操作成功的提示
     */
    @PostMapping("/updatePassword")
    public Result<Boolean> updatePassword(@RequestBody Map<String, String> params, 
                                          @RequestHeader(value = "Authorization", required = false) String token) {
        // 从 Token 中解析出当前用户名
        String username = jwtUtils.getUsernameFromToken(token);
        
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        
        boolean success = userService.updatePassword(username, oldPassword, newPassword);
        return Result.success("密码修改成功", success);
    }

    /**
     * 用户退出接口
     * HTTP 方法：POST
     * API 路径：/user/logout
     * 功能说明：将当前 Token 加入 Redis 黑名单，使其立即失效。
     * @param token 请求头中的 Authorization Token
     * @return 操作成功的 Result 对象
     */
    @PostMapping("/logout")
    public Result<Boolean> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token != null && !token.isEmpty()) {
            // 计算 Token 剩余有效期，作为黑名单的过期时间
            try {
                long expireMillis = jwtUtils.getExpirationFromToken(token).getTime() - System.currentTimeMillis();
                if (expireMillis > 0) {
                    redisTemplate.opsForValue().set(
                            LoginInterceptor.TOKEN_BLACKLIST_PREFIX + token,
                            "logout",
                            expireMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
                }
            } catch (Exception ignored) {
                // Token 解析失败时无需处理，直接返回退出成功
            }
        }
        return Result.success("退出登录成功", true);
    }

    /**
     * 管理员后台创建新用户
     * HTTP 方法：POST
     * API 路径：/user/create
     * 功能：由管理员在后台系统手动录入新员工信息。
     * 逻辑：复用注册逻辑（包含唯一性校验、密码 BCrypt 加密、默认状态填充等）。
     * 权限控制：仅拥有 'user:add' 权限的用户可访问。
     * @param user 前端传入的用户 JSON 对象
     * @return 包含新创建用户信息的 Result 对象
     */
    @PostMapping("/create")
    @CheckPermission("user:add")
    public Result<User> save(@RequestBody User user) {
        // 调用 Service 层的注册/创建逻辑，确保密码被加密且字段完整
        User newUser = userService.register(user);
        return Result.success("用户创建成功", newUser);
    }

    /**
     * 更新已有用户资料信息
     * HTTP 方法：PUT
     * 数据格式：JSON 请求体 (包含待更新的 ID 和字段)
     * @param user 待修改的用户数据
     * @return 操作是否成功的 Result 对象 (true 为成功)
     */
    @PutMapping("/edit")
    @CheckPermission("user:edit")
    public Result<Boolean> update(@RequestBody User user) {
        return Result.success(userService.update(user));
    }

    //个人信息修改
    @PutMapping("/editMyself")
    @CheckPermission("edit:myself")
    public Result<Boolean> updateMyself(@RequestBody User user) {
        return Result.success(userService.update(user));
    }

    /**
     * 为用户分配角色
     * @param userId 用户 ID
     * @param roleIds 选中的角色 ID 列表 (JSON 数组)
     */
    @PostMapping("/assignRoles/{userId}")
    public Result<Boolean> assignRoles(@PathVariable Integer userId, @RequestBody List<Integer> roleIds) {
        return Result.success("角色分配成功", userService.assignRoles(userId, roleIds));
    }

    /**
     * 根据主键物理删除该用户记录
     * HTTP 方法：DELETE
     * API 路径：/user/{id}
     * @param id 目标删除用户的 ID
     * @return 操作是否成功的 Result 对象 (true 为成功)
     */
    @DeleteMapping("/delete/{id}")
    @CheckPermission("user:delete")
    public Result<Boolean> delete(@PathVariable Integer id) {
        return Result.success(userService.deleteById(id));
    }
}

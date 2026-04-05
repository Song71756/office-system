package com.office.newofficeautomationbackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.office.newofficeautomationbackend.common.Result;
import com.office.newofficeautomationbackend.common.ResultCode;
import com.office.newofficeautomationbackend.common.annotation.CheckPermission;
import com.office.newofficeautomationbackend.common.annotation.Logical;
import com.office.newofficeautomationbackend.dto.UserDTO;
import com.office.newofficeautomationbackend.entity.Permission;
import com.office.newofficeautomationbackend.service.PermissionService;
import com.office.newofficeautomationbackend.service.UserService;
import com.office.newofficeautomationbackend.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 登录与权限拦截器
 * 负责在请求到达 Controller 之前：
 * 1. 校验请求头中的 JWT Token 是否合法且有效
 * 2. 校验当前登录用户是否拥有访问特定接口所需的权限 (@CheckPermission)
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /** Redis Key 前缀：用户权限缓存 */
    private static final String PERMISSION_CACHE_PREFIX = "user:permissions:";
    /** Redis Key 前缀：Token 黑名单 */
    public static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 检查拦截的目标是否为 Controller 的方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;


        // 2. 身份校验 (JWT Token)
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            return sendError(response, ResultCode.UNAUTHORIZED, "用户未登录，请先登录");
        }

        String username;
        try {
            if (jwtUtils.isTokenExpired(token)) {
                return sendError(response, ResultCode.UNAUTHORIZED, "登录已过期，请重新登录");
            }
            username = jwtUtils.getUsernameFromToken(token);
        } catch (Exception e) {
            return sendError(response, ResultCode.UNAUTHORIZED, "无效的身份令牌");
        }

        // 2.1 检查 Token 是否已被加入黑名单（用户已登出）
        if (Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token))) {
            return sendError(response, ResultCode.UNAUTHORIZED, "该令牌已失效，请重新登录");
        }

        // 3. 权限校验 (针对标注了 @CheckPermission 的方法)
        CheckPermission checkPermission = handlerMethod.getMethodAnnotation(CheckPermission.class);
        if (checkPermission != null) {
            String[] requiredPermissions = checkPermission.value(); // 获取要求的权限编码数组
            Logical logical = checkPermission.logical(); // 获取校验逻辑 (AND/OR)

            // 查询当前登录用户信息
            UserDTO userDTO = userService.getByUsername(username);
            if (userDTO == null) {
                return sendError(response, ResultCode.UNAUTHORIZED, "用户身份异常");
            }

            // 优先从 Redis 缓存中获取权限列表
            List<String> permissionCodes;
            Object cached = redisTemplate.opsForValue().get(PERMISSION_CACHE_PREFIX + userDTO.getId());
            if (cached instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> cachedList = (List<String>) cached;
                permissionCodes = cachedList;
            } else {
                // 缓存未命中，从数据库加载并回填缓存
                List<Permission> userPermissions = permissionService.getPermissionsByUserId(userDTO.getId());
                permissionCodes = userPermissions.stream()
                        .map(Permission::getPermissionCode)
                        .collect(Collectors.toList());
                redisTemplate.opsForValue().set(
                        PERMISSION_CACHE_PREFIX + userDTO.getId(),
                        permissionCodes,
                        24, TimeUnit.HOURS);
            }

            // 执行多权限逻辑判定
            boolean hasAuth = false;
            if (logical == Logical.AND) {
                // 且关系：必须包含数组中所有的权限
                hasAuth = true;
                for (String p : requiredPermissions) {
                    if (!permissionCodes.contains(p)) {
                        hasAuth = false;
                        break;
                    }
                }
            } else {
                // 或关系：包含其中任意一个权限即可
                for (String p : requiredPermissions) {
                    if (permissionCodes.contains(p)) {
                        hasAuth = true;
                        break;
                    }
                }
            }

            if (!hasAuth) {
                return sendError(response, ResultCode.FORBIDDEN, "权限不足，无法访问该接口");
            }
        }

        return true;
    }

    /**
     * 辅助方法：向前端发送错误 JSON 响应
     */
    private boolean sendError(HttpServletResponse response, ResultCode code, String message) throws Exception {
        response.setContentType("application/json;charset=utf-8");
        Result<Object> errorResult = Result.error(code, message);
        String json = new ObjectMapper().writeValueAsString(errorResult);
        response.getWriter().print(json);
        return false;
    }
}
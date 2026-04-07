package com.office.newofficeautomationbackend.service;

import com.office.newofficeautomationbackend.dto.LoginResponseDTO;
import com.office.newofficeautomationbackend.entity.User;
import com.office.newofficeautomationbackend.mapper.UserMapper;
import com.office.newofficeautomationbackend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * UserService 单元测试
 * 测试用户注册、登录、修改密码等核心业务逻辑
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    /**
     * 测试场景：注册时用户名已存在
     * 期望：抛出 RuntimeException，提示用户名已被占用
     */
    @Test
    void register_UserNameExists_ShouldThrowException() {
        // 准备：用户名已存在
        User existingUser = new User();
        existingUser.setUsername("zhangsan");
        when(userMapper.getByUsername("zhangsan")).thenReturn(existingUser);

        // 执行 & 断言：应该抛出异常
        User newUser = new User();
        newUser.setUsername("zhangsan");
        newUser.setPassword("123456");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.register(newUser);
        });

        assertEquals("该用户名已被占用，请更换。", exception.getMessage());
    }

    /**
     * 测试场景：登录时密码错误
     * 期望：抛出 RuntimeException，提示密码错误
     */
    @Test
    void login_WrongPassword_ShouldThrowException() {
        // 准备：用户存在，但密码不匹配
        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setUsername("zhangsan");
        // 数据库中存储的是 BCrypt 加密后的密码 "password123"
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        existingUser.setPassword(encoder.encode("password123"));
        existingUser.setStatus(1);
        when(userMapper.getByUsername("zhangsan")).thenReturn(existingUser);

        // 执行 & 断言：使用错误密码登录应该抛出异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login("zhangsan", "wrongPassword");
        });

        assertEquals("密码错误", exception.getMessage());
    }

    /**
     * 测试场景：修改密码时原密码错误
     * 期望：抛出 RuntimeException，提示原密码错误
     */
    @Test
    void updatePassword_WrongOldPassword_ShouldThrowException() {
        // 准备：原密码错误
        User existingUser = new User();
        existingUser.setId(1);
        existingUser.setUsername("zhangsan");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        existingUser.setPassword(encoder.encode("correctOldPassword"));
        when(userMapper.getByUsername("zhangsan")).thenReturn(existingUser);

        // 执行 & 断言：原密码错误应该抛出异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updatePassword("zhangsan", "wrongOldPassword", "newPassword123");
        });

        assertEquals("原密码错误，请核对后再试", exception.getMessage());
    }

    /**
     * 测试场景：用户登录时不存在
     * 期望：抛出 RuntimeException，提示用户不存在
     */
    @Test
    void login_UserNotExists_ShouldThrowException() {
        // 准备：用户不存在
        when(userMapper.getByUsername("nonexistent")).thenReturn(null);

        // 执行 & 断言：用户不存在应该抛出异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login("nonexistent", "anyPassword");
        });

        assertEquals("该用户不存在", exception.getMessage());
    }

    /**
     * 测试场景：用户登录时账号被禁用
     * 期望：抛出 RuntimeException，提示账号已被禁用
     */
    @Test
    void login_AccountDisabled_ShouldThrowException() {
        // 准备：用户存在但被禁用
        User disabledUser = new User();
        disabledUser.setId(1);
        disabledUser.setUsername("zhangsan");
        disabledUser.setPassword(new BCryptPasswordEncoder().encode("password"));
        disabledUser.setStatus(0);  // 禁用状态
        when(userMapper.getByUsername("zhangsan")).thenReturn(disabledUser);

        // 执行 & 断言：账号禁用应该抛出异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.login("zhangsan", "password");
        });

        assertEquals("该账号已被禁用", exception.getMessage());
    }

    /**
     * 测试场景：更新不存在的用户
     * 期望：抛出 RuntimeException，提示用户不存在
     */
    @Test
    void update_UserNotExists_ShouldThrowException() {
        // 准备：用户不存在
        when(userMapper.getById(999)).thenReturn(null);

        // 执行 & 断言：用户不存在应该抛出异常
        User user = new User();
        user.setId(999);
        user.setUsername("nonexistent");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.update(user);
        });

        assertEquals("用户不存在", exception.getMessage());
    }
}

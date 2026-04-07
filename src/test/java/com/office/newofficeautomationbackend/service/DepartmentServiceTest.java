package com.office.newofficeautomationbackend.service;

import com.office.newofficeautomationbackend.entity.Department;
import com.office.newofficeautomationbackend.mapper.DepartmentMapper;
import com.office.newofficeautomationbackend.service.impl.DepartmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DepartmentService 单元测试
 * 测试部门删除等核心业务逻辑
 */
@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentMapper departmentMapper;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    /**
     * 测试场景：删除部门时，部门下有子部门
     * 期望：抛出 RuntimeException，拒绝删除
     */
    @Test
    void deleteById_HasChildren_ShouldThrowException() {
        // 准备：部门下有子部门（countChildren 返回 > 0）
        when(departmentMapper.countChildren(1)).thenReturn(3);

        // 执行 & 断言：应该抛出异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            departmentService.deleteById(1);
        });

        assertEquals("该部门下存在子部门，请先删除或迁移子部门。", exception.getMessage());
        // 验证 deleteById 没有被调用（因为校验失败就抛异常了）
        verify(departmentMapper, never()).deleteById(any());
    }

    /**
     * 测试场景：删除部门时，部门下有在职员工
     * 期望：抛出 RuntimeException，拒绝删除
     */
    @Test
    void deleteById_HasEmployees_ShouldThrowException() {
        // 准备：没有子部门，但有员工（countChildren 返回 0，countUsers 返回 > 0）
        when(departmentMapper.countChildren(1)).thenReturn(0);
        when(departmentMapper.countUsers(1)).thenReturn(5);

        // 执行 & 断言：应该抛出异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            departmentService.deleteById(1);
        });

        assertEquals("该部门下仍有员工在职，无法直接删除。", exception.getMessage());
        // 验证 deleteById 没有被调用
        verify(departmentMapper, never()).deleteById(any());
    }

    /**
     * 测试场景：删除空部门（无子部门、无员工）
     * 期望：删除成功
     */
    @Test
    void deleteById_EmptyDepartment_ShouldSuccess() {
        // 准备：没有子部门，没有员工
        when(departmentMapper.countChildren(1)).thenReturn(0);
        when(departmentMapper.countUsers(1)).thenReturn(0);
        when(departmentMapper.deleteById(1)).thenReturn(1);

        // 执行
        boolean result = departmentService.deleteById(1);

        // 断言：删除成功
        assertTrue(result);
        // 验证 deleteById 被调用了一次
        verify(departmentMapper, times(1)).deleteById(1);
    }
}

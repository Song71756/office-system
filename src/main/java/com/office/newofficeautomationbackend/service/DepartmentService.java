package com.office.newofficeautomationbackend.service;

import com.office.newofficeautomationbackend.entity.Department;
import java.util.List;

/**
 * 部门管理业务逻辑接口
 * 定义了组织架构管理的核心方法，包含 CRUD 及树形结构构建
 */
public interface DepartmentService {
    /**
     * 获取所有部门的原始列表
     * @return 包含所有部门实体的集合
     */
    List<Department> list();

    /**
     * 获取全量部门树形结构
     * 自动通过递归算法将扁平的部门数据组装成层级嵌套的树
     * @return 包含根节点的部门树集合
     */
    List<Department> getTree();

    /**
     * 根据主键 ID 获取部门详细资料
     * @param id 部门唯一 ID
     * @return 匹配的部门对象
     */
    Department getById(Integer id);

    /**
     * 保存或更新部门信息
     * 如果包含 ID 则更新，否则执行插入并补全默认值
     * @param department 部门实体
     * @return 操作是否成功
     */
    boolean saveOrUpdate(Department department);

    /**
     * 根据主键 ID 删除部门
     * ⚠️ 注意：该方法会检查部门下是否有子部门或员工，若有则拒绝删除并抛出异常
     * @param id 待删除部门 ID
     * @return 是否删除成功
     */
    boolean deleteById(Integer id);
}

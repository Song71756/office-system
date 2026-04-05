package com.office.newofficeautomationbackend.mapper;

import com.office.newofficeautomationbackend.entity.Department;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 部门管理持久层接口 (MyBatis Mapper)
 * 负责与 sys_department 表进行交互，实现组织架构的 CRUD
 */
@Mapper
public interface DepartmentMapper {

    /**
     * 查询所有部门信息
     * @return 包含所有部门的 List 集合
     */
    @Select("SELECT * FROM sys_department ORDER BY order_num ASC")
    List<Department> list();

    /**
     * 根据主键 ID 获取部门详情
     * @param id 部门 ID
     * @return 部门实体对象
     */
    @Select("SELECT * FROM sys_department WHERE id = #{id}")
    Department getById(Integer id);

    /**
     * 持久化新增部门
     * 使用 @Options 获取自增 ID
     * @param department 待插入的部门实体
     * @return 受影响的行数
     */
    @Insert("INSERT INTO sys_department (name, description, parent_id, order_num, create_time, update_time) " +
            "VALUES (#{name}, #{description}, #{parentId}, #{orderNum}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Department department);

    /**
     * 更新现有部门资料
     * @param department 包含更新信息的实体 (需包含 id)
     * @return 受影响的行数
     */
    @Update("UPDATE sys_department SET name=#{name}, description=#{description}, parent_id=#{parentId}, " +
            "order_num=#{orderNum}, update_time=#{updateTime} WHERE id=#{id}")
    int update(Department department);

    /**
     * 根据 ID 删除部门记录
     * @param id 待删除部门的 ID
     * @return 受影响的行数
     */
    @Delete("DELETE FROM sys_department WHERE id = #{id}")
    int deleteById(Integer id);

    /**
     * 校验部门下是否还存在子部门
     * @param id 当前部门 ID
     * @return 子部门数量
     */
    @Select("SELECT COUNT(*) FROM sys_department WHERE parent_id = #{id}")
    int countChildren(Integer id);

    /**
     * 校验部门下是否还有关联的用户员工
     * @param id 部门 ID
     * @return 关联用户数量
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE department_id = #{id}")
    int countUsers(Integer id);
}

package com.office.newofficeautomationbackend.service;

import com.github.pagehelper.PageInfo;
import com.office.newofficeautomationbackend.entity.Document;

/**
 * 公文管理业务逻辑接口
 * 定义了公文起草、提交审批、审批流程等核心业务方法
 */
public interface DocumentService {
    /**
     * 安全分页查询公文列表
     * @param pageNum 当前页
     * @param pageSize 每页条数
     * @param keyword 搜索关键字 (标题或编号)
     * @param status 状态过滤
     * @param currentUserId 当前登录用户 ID (核心：用于实现视角隔离)
     * @param hasApprovePermission 当前用户是否拥有审批权限
     * @return 分页包装结果
     */
    PageInfo<Document> findPage(int pageNum, int pageSize, String keyword, Integer status, Integer currentUserId, boolean hasApprovePermission);

    PageInfo<Document> findMyPages(int pageNum, int pageSize, Integer currentUserId);

    /**
     * 根据主键 ID 获取公文详细资料
     * @param id 公文唯一 ID
     * @return 公文实体对象
     */
    Document getById(Integer id);

    /**
     * 起草或更新公文信息
     * 如果包含 ID 则执行更新，否则执行插入并自动补全创建人、部门和时间
     * @param document 公文实体数据
     * @param username 当前操作人用户名 (用于自动关联创建人)
     * @return 操作是否成功
     */
    boolean saveOrUpdate(Document document, String username);

    /**
     * 将公文提交进入审核流程
     * 状态变更为：审核中 (1)
     * @param id 公文 ID
     * @return 是否提交成功
     */
    boolean submit(Integer id);

    /**
     * 执行公文审批动作
     * @param id 公文 ID
     * @param status 审批后的目标状态 (2:通过, 3:驳回)
     * @param comment 审批人填写的意见或评论
     * @param approverUsername 执行审批操作的用户名
     * @return 审批是否成功
     */
    boolean approve(Integer id, Integer status, String comment, String approverUsername);

    /**
     * 物理删除指定的公文记录
     * @param id 待删除的公文 ID
     * @return 操作是否成功
     */
    boolean deleteById(Integer id);
}

package com.office.newofficeautomationbackend.service;

import com.github.pagehelper.PageInfo;
import com.office.newofficeautomationbackend.entity.Notice;

/**
 * 通知公告业务逻辑接口
 */
public interface NoticeService {
    /**
     * 分页查询通知列表
     * @param pageNum 当前页
     * @param pageSize 每页大小
     * @param keyword 搜索关键字
     * @param status 状态
     * @return 分页结果
     */
    PageInfo<Notice> findPage(int pageNum, int pageSize, String keyword, Integer status);

    /**
     * 根据 ID 获取通知详情并增加阅读量
     * @param id 通知 ID
     * @return 通知详情
     */
    Notice getById(Integer id);

    /**
     * 发布或更新通知
     * @param notice 通知实体
     * @param username 发布者用户名 (用于自动关联 ID)
     * @return 是否成功
     */
    boolean saveOrUpdate(Notice notice, String username);

    /**
     * 根据 ID 删除通知
     */
    boolean deleteById(Integer id);
}

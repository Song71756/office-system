package com.office.newofficeautomationbackend.service;

import com.github.pagehelper.PageInfo;
import com.office.newofficeautomationbackend.dto.ScheduleDTO;
import com.office.newofficeautomationbackend.entity.Schedule;

/**
 * 日程管理业务逻辑接口
 */
public interface ScheduleService {
    /**
     * 分页查询个人日程
     * @param pageNum 当前页
     * @param pageSize 每页大小
     * @param keyword 搜索关键字
     * @param status 状态
     * @param username 当前登录用户名
     * @return 分页结果
     */
    PageInfo<ScheduleDTO> findMyPage(int pageNum, int pageSize, String keyword, Integer status, String username);

    /**
     * 分页查询全部日程（不限用户）
     * @param pageNum 当前页
     * @param pageSize 每页大小
     * @param keyword 搜索关键字
     * @param status 状态
     * @return 分页结果
     */
    PageInfo<ScheduleDTO> findAllPage(int pageNum, int pageSize, String keyword, Integer status);

    /**
     * 根据 ID 获取日程详情
     */
    ScheduleDTO getById(Integer id);

    /**
     * 保存或更新日程
     * @param schedule 日程实体
     * @param username 当前操作用户名
     * @return 是否成功
     */
    boolean saveOrUpdate(Schedule schedule, String username);

    /**
     * 删除日程
     */
    boolean deleteById(Integer id);
}

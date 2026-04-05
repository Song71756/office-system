package com.office.newofficeautomationbackend.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.office.newofficeautomationbackend.dto.ScheduleDTO;
import com.office.newofficeautomationbackend.dto.UserDTO;
import com.office.newofficeautomationbackend.entity.Schedule;
import com.office.newofficeautomationbackend.mapper.ScheduleMapper;
import com.office.newofficeautomationbackend.service.ScheduleService;
import com.office.newofficeautomationbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 日程管理业务实现类
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private UserService userService;

    @Override
    public PageInfo<ScheduleDTO> findMyPage(int pageNum, int pageSize, String keyword, Integer status, String username) {
        // 1. 获取当前用户信息
        UserDTO user = userService.getByUsername(username);
        Integer userId = (user != null) ? user.getId() : -1;
        String realName = (user != null) ? user.getRealName() : "";

        // 2. 分页查询（同时用 username 和 realName 匹配参与人）
        PageHelper.startPage(pageNum, pageSize);
        List<Schedule> list = scheduleMapper.selectPage(userId, username, realName, keyword, status);
        PageInfo<Schedule> pageInfo = new PageInfo<>(list);
        // 3. 转换为 DTO 并填充创建人信息
        return convertPageInfo(pageInfo);
    }

    @Override
    public PageInfo<ScheduleDTO> findAllPage(int pageNum, int pageSize, String keyword, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        List<Schedule> list = scheduleMapper.selectAllPage(keyword, status);
        PageInfo<Schedule> pageInfo = new PageInfo<>(list);
        return convertPageInfo(pageInfo);
    }

    /**
     * 将 Schedule 分页结果转换为 ScheduleDTO 分页结果，并填充创建人信息
     */
    private PageInfo<ScheduleDTO> convertPageInfo(PageInfo<Schedule> pageInfo) {
        List<ScheduleDTO> dtoList = new ArrayList<>();
        for (Schedule schedule : pageInfo.getList()) {
            dtoList.add(toDTO(schedule));
        }
        PageInfo<ScheduleDTO> dtoPageInfo = new PageInfo<>();
        dtoPageInfo.setList(dtoList);
        dtoPageInfo.setTotal(pageInfo.getTotal());
        dtoPageInfo.setPageNum(pageInfo.getPageNum());
        dtoPageInfo.setPageSize(pageInfo.getPageSize());
        dtoPageInfo.setPages(pageInfo.getPages());
        return dtoPageInfo;
    }

    /**
     * 将 Schedule 实体转换为 ScheduleDTO，并填充创建人信息
     */
    private ScheduleDTO toDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(schedule.getId());
        dto.setTitle(schedule.getTitle());
        dto.setContent(schedule.getContent());
        dto.setType(schedule.getType());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setUserId(schedule.getUserId());
        dto.setPriority(schedule.getPriority());
        dto.setRemindType(schedule.getRemindType());
        dto.setStatus(schedule.getStatus());
        dto.setIsAllDay(schedule.getIsAllDay());
        dto.setLocation(schedule.getLocation());
        dto.setParticipants(schedule.getParticipants());
        dto.setCreateTime(schedule.getCreateTime());
        dto.setUpdateTime(schedule.getUpdateTime());
        // 填充创建人信息
        if (schedule.getUserId() != null) {
            UserDTO creator = userService.getById(schedule.getUserId());
            if (creator != null) {
                dto.setUsername(creator.getUsername());
                dto.setRealName(creator.getRealName());
            }
        }
        return dto;
    }

    @Override
    public ScheduleDTO getById(Integer id) {
        Schedule schedule = scheduleMapper.getById(id);
        if (schedule == null) return null;
        return toDTO(schedule);
    }

    /**
     * 保存或更新日程
     * 逻辑说明：
     * 1. 如果是新增，自动关联当前登录用户。
     * 2. 如果是修改，需校验权限：只有日程所有者或管理员才能修改。
     */
    @Override
    public boolean saveOrUpdate(Schedule schedule, String username) {
        // 获取当前操作人详情
        UserDTO currentUser = userService.getByUsername(username);
        if (currentUser == null) throw new RuntimeException("操作人不存在");

        if (schedule.getId() == null) {
            // 1. 新增逻辑
            schedule.setUserId(currentUser.getId());
            
            // 初始值
            if (schedule.getStatus() == null) {
                schedule.setStatus(0); // 未开始
            }
            if (schedule.getIsAllDay() == null) {
                schedule.setIsAllDay(0); // 默认非全天
            }
            
            schedule.setCreateTime(LocalDateTime.now());
            schedule.setUpdateTime(LocalDateTime.now());
            return scheduleMapper.insert(schedule) > 0;
        } else {
            // 2. 更新逻辑 (安全校验)
            Schedule oldSchedule = scheduleMapper.getById(schedule.getId());
            if (oldSchedule == null) throw new RuntimeException("日程记录不存在");
            
            // 业务约束：普通员工只能修改属于自己的日程
            // (如果是管理员角色可以跳过此限制，目前暂按所有者校验)
            if (!oldSchedule.getUserId().equals(currentUser.getId())) {
                throw new RuntimeException("权限不足：您无权修改他人的日程安排");
            }

            schedule.setUpdateTime(LocalDateTime.now());
            return scheduleMapper.update(schedule) > 0;
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        // 此处建议也增加所有者校验逻辑
        return scheduleMapper.deleteById(id) > 0;
    }
}

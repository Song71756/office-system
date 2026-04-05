package com.office.newofficeautomationbackend.controller;

import com.github.pagehelper.PageInfo;
import com.office.newofficeautomationbackend.common.Result;
import com.office.newofficeautomationbackend.common.annotation.CheckPermission;
import com.office.newofficeautomationbackend.common.annotation.Logical;
import com.office.newofficeautomationbackend.dto.ScheduleDTO;
import com.office.newofficeautomationbackend.entity.Schedule;
import com.office.newofficeautomationbackend.service.ScheduleService;
import com.office.newofficeautomationbackend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 日程管理控制层
 * 提供个人工作计划的增删改查接口
 */
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 分页查询当前登录用户的日程
     * @param pageNum 当前页
     * @param pageSize 每页条数
     * @param keyword 标题模糊搜索
     * @param status 状态 (0:未开始, 1:进行中, 2:已完成)
     */
    @GetMapping("/myPage")
    @CheckPermission("schedule:view")
    public Result<PageInfo<ScheduleDTO>> findMyPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestHeader("Authorization") String token) {
        
        String username = jwtUtils.getUsernameFromToken(token);
        return Result.success(scheduleService.findMyPage(pageNum, pageSize, keyword, status, username));
    }

    /**
     * 分页查询全部日程（不限用户）
     * @param pageNum 当前页
     * @param pageSize 每页条数
     * @param keyword 标题模糊搜索
     * @param status 状态过滤
     */
    @GetMapping("/page")
    @CheckPermission("schedule:view")
    public Result<PageInfo<ScheduleDTO>> findAllPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(scheduleService.findAllPage(pageNum, pageSize, keyword, status));
    }

    /**
     * 获取日程详情
     */
    @GetMapping("/{id}")
    @CheckPermission("schedule:view")
    public Result<ScheduleDTO> getById(@PathVariable Integer id) {
        return Result.success(scheduleService.getById(id));
    }

    /**
     * 保存或修改日程
     * 权限要求：schedule:create 或 schedule:edit
     */
    @PostMapping("/save")
    @CheckPermission(value = {"schedule:create", "schedule:edit"}, logical = Logical.OR)
    public Result<Boolean> save(@RequestBody Schedule schedule,
                                @RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUsernameFromToken(token);
        return Result.success("日程保存成功", scheduleService.saveOrUpdate(schedule, username));
    }

    /**
     * 删除日程
     * 权限要求：schedule:delete
     */
    @DeleteMapping("/delete/{id}")
    @CheckPermission("schedule:delete")
    public Result<Boolean> delete(@PathVariable Integer id) {
        return Result.success("日程删除成功", scheduleService.deleteById(id));
    }
}

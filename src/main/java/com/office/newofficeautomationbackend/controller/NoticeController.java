package com.office.newofficeautomationbackend.controller;

import com.github.pagehelper.PageInfo;
import com.office.newofficeautomationbackend.common.Result;
import com.office.newofficeautomationbackend.common.annotation.CheckPermission;
import com.office.newofficeautomationbackend.entity.Notice;
import com.office.newofficeautomationbackend.service.NoticeService;
import com.office.newofficeautomationbackend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 通知公告控制层
 */
@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 分页查询通知列表
     * @param pageNum 当前页
     * @param pageSize 每页条数
     * @param keyword 搜索关键字 (标题)
     * @param status 状态 (0:草稿, 1:已发布)
     */
    @GetMapping("/page")
    public Result<PageInfo<Notice>> findPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(noticeService.findPage(pageNum, pageSize, keyword, status));
    }

    /**
     * 获取通知详情
     * 注意：调用此接口会触发阅读量 +1
     */
    @GetMapping("/{id}")
    public Result<Notice> getById(@PathVariable Integer id) {
        return Result.success(noticeService.getById(id));
    }

    /**
     * 发布或保存通知
     * 权限要求：notice:publish
     */
    @PostMapping("/save")
    @CheckPermission("notice:publish")
    public Result<Boolean> save(@RequestBody Notice notice, 
                                @RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUsernameFromToken(token);
        return Result.success("操作成功", noticeService.saveOrUpdate(notice, username));
    }

    /**
     * 删除通知
     * 权限要求：notice:delete
     */
    @DeleteMapping("/delete/{id}")
    @CheckPermission("notice:delete")
    public Result<Boolean> delete(@PathVariable Integer id) {
        return Result.success("删除成功", noticeService.deleteById(id));
    }
}

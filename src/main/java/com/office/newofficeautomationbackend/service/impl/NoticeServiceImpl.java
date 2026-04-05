package com.office.newofficeautomationbackend.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.office.newofficeautomationbackend.dto.UserDTO;
import com.office.newofficeautomationbackend.entity.Notice;
import com.office.newofficeautomationbackend.entity.User;
import com.office.newofficeautomationbackend.mapper.NoticeMapper;
import com.office.newofficeautomationbackend.service.NoticeService;
import com.office.newofficeautomationbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知公告业务实现类
 */
@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Autowired
    private UserService userService;

    @Override
    public PageInfo<Notice> findPage(int pageNum, int pageSize, String keyword, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        List<Notice> list = noticeMapper.selectPage(keyword, status);
        return new PageInfo<>(list);
    }

    /**
     * 实现：获取详情并增加阅读量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Notice getById(Integer id) {
        Notice notice = noticeMapper.getById(id);
        if (notice != null) {
            noticeMapper.addViewCount(id);
            // 同步更新返回对象的查看次数，避免前端再次查询
            notice.setViewCount(notice.getViewCount() + 1);
        }
        return notice;
    }

    /**
     * 实现：发布或更新逻辑
     * 自动处理发布人关联及状态转换
     */
    @Override
    public boolean saveOrUpdate(Notice notice, String username) {
        if (notice.getId() == null) {
            // 新增逻辑
            // 1. 获取发布人 ID
            UserDTO user = userService.getByUsername(username);
            if (user != null) {
                notice.setPublisherId(user.getId());
            }
            
            // 2. 初始化字段
            notice.setViewCount(0);
            notice.setCreateTime(LocalDateTime.now());
            notice.setUpdateTime(LocalDateTime.now());
            
            // 3. 如果状态为已发布(1)，则设置发布时间
            if (Integer.valueOf(1).equals(notice.getStatus())) {
                notice.setPublishTime(LocalDateTime.now());
            }
            
            return noticeMapper.insert(notice) > 0;
        } else {
            // 更新逻辑
            notice.setUpdateTime(LocalDateTime.now());
            
            // 如果原本是草稿(0)现在改为发布(1)，补全发布时间
            Notice oldNotice = noticeMapper.getById(notice.getId());
            if (oldNotice != null && Integer.valueOf(0).equals(oldNotice.getStatus()) 
                    && Integer.valueOf(1).equals(notice.getStatus())) {
                notice.setPublishTime(LocalDateTime.now());
            }
            
            return noticeMapper.update(notice) > 0;
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        return noticeMapper.deleteById(id) > 0;
    }
}

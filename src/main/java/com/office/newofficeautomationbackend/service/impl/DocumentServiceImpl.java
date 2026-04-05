package com.office.newofficeautomationbackend.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.office.newofficeautomationbackend.dto.UserDTO;
import com.office.newofficeautomationbackend.entity.Document;
import com.office.newofficeautomationbackend.entity.User;
import com.office.newofficeautomationbackend.mapper.DocumentMapper;
import com.office.newofficeautomationbackend.service.DocumentService;
import com.office.newofficeautomationbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公文管理业务层实现类
 */
@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private DocumentMapper documentMapper;

    @Autowired
    private UserService userService;

    @Override
    public PageInfo<Document> findPage(int pageNum, int pageSize, String keyword, Integer status, Integer currentUserId, boolean hasApprovePermission) {
        PageHelper.startPage(pageNum, pageSize);
        // 使用带有“视角隔离”逻辑的 SQL 查询
        List<Document> list = documentMapper.selectVisiblePage(keyword, status, currentUserId, hasApprovePermission);
        return new PageInfo<>(list);
    }

    public PageInfo<Document> findMyPages(int pageNum, int pageSize, Integer currentUserId) {
        PageHelper.startPage(pageNum, pageSize);
        List<Document> list = documentMapper.selectByCurrentUserId(currentUserId);
        return new PageInfo<>(list);
    }

    @Override
    public Document getById(Integer id) {
        return documentMapper.getById(id);
    }

    /**
     * 实现：起草或更新逻辑
     * 如果是新增公文，会自动关联当前用户为创建人，并设置初始状态为草稿
     */
    @Override
    public boolean saveOrUpdate(Document document, String username) {
        if (document.getId() == null) {
            // 1. 新增逻辑 (起草)
            UserDTO user = userService.getByUsername(username);
            if (user != null) {
                document.setCreatorId(user.getId());
                // 默认将公文归属于创建人所在的部门
                document.setDepartmentId(user.getDepartmentId());
            }
            
            // 2. 自动生成公文编号 (如果前端未指定)
            // 格式：[年份]-类型-序号 (如: [2026]-通知-001)
            if (document.getDocNumber() == null || document.getDocNumber().trim().isEmpty()) {
                int year = LocalDateTime.now().getYear();
                String type = (document.getType() != null) ? document.getType() : "DOC";
                int nextSerial = documentMapper.countTotal() + 1;
                // 格式化序号为 3 位数字，如 001
                String docNumber = String.format("[%d]-%s-%03d", year, type, nextSerial);
                document.setDocNumber(docNumber);
            }
            
            // 3. 初始化状态为 0 (草稿)
            if (document.getStatus() == null) {
                document.setStatus(0);
            }
            
            // 3. 时间维护
            document.setCreateTime(LocalDateTime.now());
            document.setUpdateTime(LocalDateTime.now());
            
            return documentMapper.insert(document) > 0;
        } else {
            // 4. 更新逻辑
            document.setUpdateTime(LocalDateTime.now());
            return documentMapper.update(document) > 0;
        }
    }

    /**
     * 实现：提交审核
     * 仅将状态变更为 1 (审核中)
     */
    @Override
    public boolean submit(Integer id) {
        return documentMapper.updateApproveStatus(id, 1, null, null, null) > 0;
    }

    /**
     * 实现：公文审批流程
     * 使用事务保证审批记录的一致性
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Integer id, Integer status, String comment, String approverUsername) {
        // 1. 获取审批人详细信息
        UserDTO user = userService.getByUsername(approverUsername);
        Integer approverId = (user != null) ? user.getId() : null;
        
        // 2. 执行状态更新
        return documentMapper.updateApproveStatus(id, status, approverId, comment, LocalDateTime.now()) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        return documentMapper.deleteById(id) > 0;
    }
}

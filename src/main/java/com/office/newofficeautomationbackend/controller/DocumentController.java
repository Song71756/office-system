package com.office.newofficeautomationbackend.controller;

import com.github.pagehelper.PageInfo;
import com.office.newofficeautomationbackend.common.Result;
import com.office.newofficeautomationbackend.common.annotation.CheckPermission;
import com.office.newofficeautomationbackend.common.annotation.Logical;
import com.office.newofficeautomationbackend.dto.UserDTO;
import com.office.newofficeautomationbackend.entity.Document;
import com.office.newofficeautomationbackend.entity.User;
import com.office.newofficeautomationbackend.entity.Permission;
import com.office.newofficeautomationbackend.service.DocumentService;
import com.office.newofficeautomationbackend.service.PermissionService;
import com.office.newofficeautomationbackend.service.UserService;
import com.office.newofficeautomationbackend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 公文管理控制层
 * 提供公文起草、流转、审批及查询的全套 RESTful 接口
 */
@RestController
@RequestMapping("/document")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 安全分页获取公文列表
     * 自动实现“视角隔离”：
     * 1. 任何人都可以看到“已通过 (status=2)”的公文。
     * 2. 创建者本人可以看到自己起草的所有公文 (含草稿、审核中、已驳回)。
     * @param pageNum 当前页
     * @param pageSize 每页条数
     * @param keyword 搜索关键字 (标题或编号)
     * @param status 状态过滤 (可选)
     * @param token 请求头中的 Authorization (由拦截器保证有效)
     */
    @GetMapping("/page")
    @CheckPermission("document:list")
    public Result<PageInfo<Document>> findPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestHeader("Authorization") String token) {
        
        // 1. 从 Token 中解析用户名，进而获取用户 ID
        String username = jwtUtils.getUsernameFromToken(token);
        UserDTO currentUser = userService.getByUsername(username);
        Integer currentUserId = (currentUser != null) ? currentUser.getId() : null;

        // 2. 判断当前用户是否拥有审批权限
        boolean hasApprovePermission = false;
        if (currentUser != null) {
            List<Permission> perms = permissionService.getPermissionsByUserId(currentUser.getId());
            hasApprovePermission = perms.stream()
                    .anyMatch(p -> "document:approve".equals(p.getPermissionCode()));
        }

        // 3. 调用带"视角隔离"逻辑的 Service 方法
        PageInfo<Document> pageData = documentService.findPage(pageNum, pageSize, keyword, status, currentUserId, hasApprovePermission);
        
        return Result.success("获取公文列表成功", pageData);
    }

    /**
     * 分页获取我的公文
     */
    @GetMapping("/mypage")
    public Result<PageInfo<Document>> findMyPages(@RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                                  @RequestHeader("Authorization") String token) {
        // 从 Token 中解析用户名，进而获取用户 ID
        String username = jwtUtils.getUsernameFromToken(token);
        UserDTO currentUser = userService.getByUsername(username);
        Integer currentUserId = (currentUser != null) ? currentUser.getId() : null;
        return Result.success(documentService.findMyPages(pageNum, pageSize, currentUserId));
    }



    /**
     * 获取指定 ID 的公文详细资料
     */
    @GetMapping("/{id}")
    @CheckPermission("document:list")
    public Result<Document> getById(@PathVariable Integer id) {
        return Result.success(documentService.getById(id));
    }

    /**
     * 起草或修改公文
     * 逻辑：如果 ID 为空则新增，否则修改。新增时会自动关联当前登录用户为创建者。
     * 权限要求：document:create 或 document:edit
     */
    @PostMapping("/save")
    @CheckPermission(value = {"document:create", "document:edit"}, logical = Logical.OR)
    public Result<Boolean> save(@RequestBody Document document,
                                @RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUsernameFromToken(token);
        return Result.success("公文保存成功", documentService.saveOrUpdate(document, username));
    }

    /**
     * 起草或修改个人公文
     * 逻辑：如果 ID 为空则新增，否则修改。新增时会自动关联当前登录用户为创建者。
     * 权限要求：document:create:myself 或 document:edit:myself
     */
    @PostMapping("/save/myself")
    @CheckPermission(value = {"document:create:myself", "document:edit:myself"}, logical = Logical.OR)
    public Result<Boolean> saveMyDocument(@RequestBody Document document,
                                @RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUsernameFromToken(token);
        return Result.success("公文保存成功", documentService.saveOrUpdate(document, username));
    }

    /**
     * 提交公文申请
     * 将公文状态从“草稿 (0)”变更为“审核中 (1)”
     * 权限要求：document:edit
     */
    @PostMapping("/submit/{id}")
    @CheckPermission(value = {"document:edit","document:edit:myself"},logical =  Logical.OR)
    public Result<Boolean> submit(@PathVariable Integer id) {
        return Result.success("已成功提交审核", documentService.submit(id));
    }

    /**
     * 审批公文
     * 由具有审批权限的人员执行，可选择通过 (2) 或驳回 (3)
     * 权限要求：document:approve
     * @param id 公文 ID
     * @param params JSON 对象，包含：status (目标状态), approveComment (审批意见)
     */
    @PostMapping("/approve/{id}")
    @CheckPermission("document:approve")
    public Result<Boolean> approve(@PathVariable Integer id,
                                   @RequestBody Map<String, Object> params,
                                   @RequestHeader("Authorization") String token) {
        Integer status = (Integer) params.get("status");
        String approveComment = (String) params.get("approveComment");
        String username = jwtUtils.getUsernameFromToken(token);
        
        return Result.success("审批操作已完成", documentService.approve(id, status, approveComment, username));
    }

    /**
     * 物理删除公文记录
     * 权限要求：document:delete
     */
    @DeleteMapping("/delete/{id}")
    @CheckPermission("document:delete")
    public Result<Boolean> delete(@PathVariable Integer id) {
        return Result.success("删除成功", documentService.deleteById(id));
    }

    /**
     * 物理删除个人公文记录
     * 权限要求：document:delete:myself
     */
    @DeleteMapping("/delete/myself/{id}")
    @CheckPermission("document:delete:myself")
    public Result<Boolean> deleteMyDocument(@PathVariable Integer id) {
        return Result.success("删除成功", documentService.deleteById(id));
    }
}

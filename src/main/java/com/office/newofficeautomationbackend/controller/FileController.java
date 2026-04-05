package com.office.newofficeautomationbackend.controller;

import com.github.pagehelper.PageInfo;
import com.office.newofficeautomationbackend.common.Result;
import com.office.newofficeautomationbackend.common.annotation.CheckPermission;
import com.office.newofficeautomationbackend.entity.File;
import com.office.newofficeautomationbackend.service.FileService;
import com.office.newofficeautomationbackend.utils.JwtUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * 文件管理控制层
 * 提供头像、办公文件上传，以及文件数据库记录的分页查询与物理删除功能
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${file.avatar-path}")
    private String avatarPath;

    @Value("${file.office-path}")
    private String officePath;

    /**
     * 上传头像接口
     * 自动存入 /uploads/avatar/ 目录
     */
    @PostMapping("/upload/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file,
                                       @RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, String> result = fileService.processUpload(file, avatarPath, "/uploads/avatar/", token, true);
        return Result.success("文件上传并入库成功", result);
    }

    /**
     * 上传办公文件接口
     * 自动存入 /uploads/office/ 目录
     */
    @PostMapping("/upload/office")
    public Result<Map<String, String>> uploadOffice(@RequestParam("file") MultipartFile file,
                                             @RequestHeader(value = "Authorization", required = false) String token) {
        Map<String,String> result = fileService.processUpload(file, officePath, "/uploads/office/", token, false);
        return Result.success("文件上传并入库成功", result);
    }

    /**
     * 兼容旧版上传接口，默认存入办公目录
     */
    @PostMapping("/upload")
    public Result<Map<String,String>> upload(@RequestParam("file") MultipartFile file,
                                 @RequestHeader(value = "Authorization", required = false) String token) {
        return uploadOffice(file, token);
    }

    /**
     * 安全下载文件接口
     * 功能：根据 ID 查库，获取原始文件名并恢复，同时更新下载次数
     * @param id 文件记录 ID
     */
    @GetMapping("/download/{id}")
    public void download(@PathVariable Integer id, HttpServletResponse response) {
        try {
            File fileRecord = fileService.getById(id);
            if (fileRecord == null) {
                response.setStatus(404);
                return;
            }

            String absolutePathStr = "E:/TheFinishedHomwork" + fileRecord.getFilePath();
            Path path = Paths.get(absolutePathStr);
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                String downloadName = fileRecord.getOriginalName();
                if (downloadName == null) downloadName = fileRecord.getFileName();
                
                String encodedName = URLEncoder.encode(downloadName, StandardCharsets.UTF_8.name())
                        .replaceAll("\\+", "%20");

                response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedName + "\"");
                
                Files.copy(path, response.getOutputStream());
                fileService.addDownloadCount(id);
            } else {
                response.setStatus(404);
            }
        } catch (Exception e) {
            response.setStatus(500);
        }
    }

    /**
     * 物理删除文件
     * 同时移除数据库记录与磁盘物理文件
     * 权限要求：file:delete
     */
    @DeleteMapping("/delete/{id}")
    @CheckPermission("file:delete")
    public Result<Boolean> delete(@PathVariable Integer id) {
        return Result.success("物理删除成功", fileService.deleteWithFile(id));
    }

    /**
     * 根据新文件名物理删除文件
     * 同时移除数据库记录与磁盘物理文件
     * 权限要求：file:delete
     */
    @DeleteMapping("/delete/newFileName")
    @CheckPermission("file:delete")
    public Result<Boolean> deleteByNewFileName(@RequestParam String newFileName) {
        return Result.success("物理删除成功", fileService.deleteByNewFileName(newFileName));
    }

    /**
     * 获取层级化文件列表 (个人视图)
     * @param parentId 父文件夹 ID (0 为根目录)
     */
    @GetMapping("/myFiles")
    public Result<PageInfo<File>> findMyFiles(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer parentId,
            @RequestParam(required = false) String keyword,
            @RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUsernameFromToken(token);
        return Result.success(fileService.findMyFiles(pageNum, pageSize, parentId, keyword, username));
    }

    /**
     * 创建新文件夹
     */
    @PostMapping("/folder")
    @CheckPermission("file:edit")
    public Result<File> createFolder(
            @RequestBody Map<String, Object> params,
            @RequestHeader("Authorization") String token) {
        String folderName = (String) params.get("folderName");
        Integer parentId = (Integer) params.get("parentId");
        String username = jwtUtils.getUsernameFromToken(token);
        return Result.success("文件夹创建成功", fileService.createFolder(folderName, parentId, username));
    }

    /**
     * 重命名文件或文件夹
     */
    @PutMapping("/rename")
    @CheckPermission("file:edit")
    public Result<Boolean> rename(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String newName = (String) params.get("newName");
        return Result.success("重命名成功", fileService.rename(id, newName));
    }

    /**
     * 移动文件位置
     */
    @PutMapping("/move")
    @CheckPermission("file:edit")
    public Result<Boolean> move(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        Integer targetParentId = (Integer) params.get("targetParentId");
        return Result.success("移动成功", fileService.move(id, targetParentId));
    }

    /**
     * 辅助方法：将字节大小转换为可读字符串 (如 1.2MB)
     */
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        int z = (63 - Long.numberOfLeadingZeros(size)) / 10;
        return String.format("%.1f %sB", (double)size / (1L << (z * 10)), " KMGTPE".charAt(z));
    }
}

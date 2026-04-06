package com.office.newofficeautomationbackend.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.office.newofficeautomationbackend.dto.UserDTO;
import com.office.newofficeautomationbackend.entity.File;
import com.office.newofficeautomationbackend.entity.User;
import com.office.newofficeautomationbackend.mapper.FileMapper;
import com.office.newofficeautomationbackend.service.FileService;
import com.office.newofficeautomationbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.office.newofficeautomationbackend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;

/**
 * 文件管理业务逻辑实现类
 */
@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${file.base-path}")
    private String basePath;

    @Override
    public boolean save(File file) {
        return fileMapper.insert(file) > 0;
    }

    @Override
    public PageInfo<File> findPage(int pageNum, int pageSize, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<File> list = fileMapper.selectByKeyword(keyword);
        return new PageInfo<>(list);
    }

    /**
     * 实现：分层级查询个人文件
     */
    @Override
    public PageInfo<File> findMyFiles(int pageNum, int pageSize, Integer parentId, String keyword, String username) {
        UserDTO user = userService.getByUsername(username);
        Integer userId = (user != null) ? user.getId() : -1;
        
        PageHelper.startPage(pageNum, pageSize);
        // 根目录 parentId 默认为 null
        List<File> list = fileMapper.selectByParentId(userId, parentId, keyword);
        return new PageInfo<>(list);
    }



    /**
     * 实现：创建虚拟文件夹记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public File createFolder(String folderName, Integer parentId, String username) {
        UserDTO user = userService.getByUsername(username);
        
        File folder = new File();
        folder.setOriginalName(folderName);
        folder.setFileName(folderName); // 文件夹记录不需要物理文件名
        folder.setFileType("folder");
        folder.setParentId(parentId==0?null:parentId);
        folder.setUploaderId(user != null ? user.getId() : null);
        folder.setFileSize(0L);
        folder.setDownloadCount(0);
        folder.setIsPublic(0);
        folder.setCreateTime(LocalDateTime.now());
        folder.setUpdateTime(LocalDateTime.now());
        
        fileMapper.insert(folder);
        return folder;
    }

    /**
     * 实现：重命名
     */
    @Override
    public boolean rename(Integer id, String newName) {
        File file = fileMapper.getById(id);
        if (file == null) return false;
        file.setOriginalName(newName);
        file.setUpdateTime(LocalDateTime.now());
        return fileMapper.update(file) > 0;
    }

    /**
     * 实现：移动位置
     */
    @Override
    public boolean move(Integer id, Integer targetParentId) {
        File file = fileMapper.getById(id);
        if (file == null) return false;
        file.setParentId(targetParentId);
        file.setUpdateTime(LocalDateTime.now());
        return fileMapper.update(file) > 0;
    }

    @Override
    public File getById(Integer id) {
        return fileMapper.getById(id);
    }

    @Override
    public boolean deleteById(Integer id) {
        return fileMapper.deleteById(id) > 0;
    }

    /**
     * 实现：物理删除逻辑
     * 1. 查库获取文件路径
     * 2. 删除磁盘物理文件（先删，失败则整个回滚）
     * 3. 删除数据库记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteWithFile(Integer id) {
        // 1. 获取文件记录详情
        File file = fileMapper.getById(id);
        if (file == null) {
            throw new RuntimeException("文件记录不存在，无法执行物理删除");
        }

        // 2. 先删除物理文件（如果失败，数据库还没动，不需要回滚）
        try {
            String absolutePathStr = basePath + file.getFilePath();
            Path path = Paths.get(absolutePathStr);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("磁盘文件删除失败: " + e.getMessage());
        }

        // 3. 物理文件删除成功后，再删除数据库记录
        int rows = fileMapper.deleteById(id);
        if (rows <= 0) {
            return false;
        }
        return true;
    }



    /**
     * 实现：物理删除逻辑（按新文件名）
     * 1. 查库获取文件路径
     * 2. 删除磁盘物理文件（先删，失败则整个回滚）
     * 3. 删除数据库记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByNewFileName(String newFileName) {
        // 1. 获取文件记录详情
        File file = fileMapper.getByNewFileName(newFileName);
        if (file == null) {
            throw new RuntimeException("文件记录不存在，无法执行物理删除");
        }

        // 2. 先删除物理文件（如果失败，数据库还没动，不需要回滚）
        try {
            String absolutePathStr = basePath + file.getFilePath();
            Path path = Paths.get(absolutePathStr);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("磁盘文件删除失败: " + e.getMessage());
        }

        // 3. 物理文件删除成功后，再删除数据库记录
        int rows = fileMapper.deleteByNewFileName(newFileName);
        if (rows <= 0) {
            return false;
        }
        return true;
    }



    @Override
    public void addDownloadCount(Integer id) {
        fileMapper.updateDownloadCount(id);
    }

    /**
     * 核心上传逻辑
     * 将文件写入磁盘、保存元数据到 file 表，若为头像上传则同步更新 user 表的 avatar 字段
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String,String> processUpload(MultipartFile file, String physicalDir, String urlPrefix, String token, boolean isAvatar) {
        Map<String,String> result = new HashMap<>();
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        try {
            java.io.File dir = new java.io.File(physicalDir);
            if (!dir.exists()) dir.mkdirs();

            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + extension;

            Path path = Paths.get(physicalDir + newFileName);
            Files.write(path, file.getBytes());

            String fileUrl = urlPrefix + newFileName;

            File fileRecord = new File();
            fileRecord.setFileName(newFileName);
            fileRecord.setOriginalName(originalName);
            fileRecord.setFilePath(fileUrl);
            fileRecord.setFileSize(file.getSize());
            fileRecord.setFileType(extension.toLowerCase());
            fileRecord.setMimeType(file.getContentType());
            fileRecord.setCreateTime(LocalDateTime.now());
            fileRecord.setUpdateTime(LocalDateTime.now());
            fileRecord.setDownloadCount(0);
            fileRecord.setShareType(0);
            fileRecord.setIsPublic(0);
            fileRecord.setParentId(null);

            UserDTO user = null;
            if (token != null) {
                String username = jwtUtils.getUsernameFromToken(token);
                user = userService.getByUsername(username);
                if (user != null) fileRecord.setUploaderId(user.getId());
            }

            fileMapper.insert(fileRecord);

            // 头像上传时，仅更新 user 表的 avatar 字段，避免覆盖其他字段
            if (isAvatar && user != null) {
                userService.updateAvatar(user.getId(), fileUrl);
            }
            result.put("newFileName", newFileName);
            result.put("originalName", originalName);
            result.put("fileUrl", fileUrl);

            return result;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }
}

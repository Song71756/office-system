package com.office.newofficeautomationbackend.service;

import com.github.pagehelper.PageInfo;
import com.office.newofficeautomationbackend.entity.File;

import java.util.Map;

/**
 * 文件管理业务逻辑接口
 */
public interface FileService {

    /**
     * 保存文件元数据
     * @param file 文件信息
     * @return 是否保存成功
     */
    boolean save(File file);

    /**
     * 分页查询文件列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keyword 搜索关键词
     * @return 分页结果
     */
    PageInfo<File> findPage(int pageNum, int pageSize, String keyword);

    /**
     * 根据 ID 获取文件详情
     */
    File getById(Integer id);

    /**
     * 分页查询个人文件和文件夹 (按层级)
     * @param pageNum 当前页
     * @param pageSize 每页条数
     * @param parentId 父文件夹 ID (0 为根目录)
     * @param keyword 搜索关键字
     * @param username 当前登录人
     */
    PageInfo<File> findMyFiles(int pageNum, int pageSize, Integer parentId, String keyword, String username);

    /**
     * 创建文件夹
     * @param folderName 文件夹名
     * @param parentId 父文件夹 ID
     * @param username 当前登录人
     */
    File createFolder(String folderName, Integer parentId, String username);

    /**
     * 重命名文件或文件夹
     */
    boolean rename(Integer id, String newName);

    /**
     * 移动文件或文件夹
     */
    boolean move(Integer id, Integer targetParentId);

    /**
     * 实现物理删除：不仅从数据库删除记录，还会同步删除磁盘上的物理文件
     * @param id 待删除的文件唯一 ID
     * @return 操作是否成功 (true 为成功)
     */
    boolean deleteWithFile(Integer id);

    /**
     * 根据新文件名实现物理删除：不仅从数据库删除记录，还会同步删除磁盘上的物理文件
     * @param newFileName 待删除的文件的新文件名
     * @return 操作是否成功 (true 为成功)
     */
    boolean deleteByNewFileName(String newFileName);

    /**
     * 根据 ID 仅删除数据库记录
     */
    boolean deleteById(Integer id);

    /**
     * 增加下载次数
     */
    void addDownloadCount(Integer id);

    /**
     * 核心上传逻辑
     * @param file 原始文件对象
     * @param physicalDir 物理存储主目录
     * @param urlPrefix 网络访问路径前缀
     * @param token 用于解析上传人身份的令牌
     * @param isAvatar 是否为头像上传（若为 true，则同步更新用户头像路径）
     * @return 上传后的文件访问 URL
     */
    Map<String, String> processUpload(org.springframework.web.multipart.MultipartFile file, String physicalDir, String urlPrefix, String token, boolean isAvatar);
}

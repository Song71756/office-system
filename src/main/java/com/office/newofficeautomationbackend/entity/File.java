package com.office.newofficeautomationbackend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class File {
    /**
     * 文件唯一标识 ID
     */
    private Integer id;

    /**
     * 加密后的存储文件名 (如 UUID.jpg)
     */
    private String fileName;

    /**
     * 用户上传时的原始文件名
     */
    private String originalName;

    /**
     * 文件的网络访问路径或相对存储路径
     */
    private String filePath;

    /**
     * 文件大小 (单位：字节，对应数据库 BIGINT)
     */
    private Long fileSize;

    /**
     * 文件后缀名 (如 .jpg, .pdf)
     */
    private String fileType;

    /**
     * 文件的 MIME 类型 (如 image/jpeg)
     */
    private String mimeType;

    /**
     * 分享类型 (0:私有, 1:部分可见, 2:公开)
     */
    private Integer shareType;

    /**
     * 上传该文件的用户 ID
     */
    private Integer uploaderId;

    /**
     * 文件被累计下载的次数
     */
    private Integer downloadCount;

    /**
     * 是否公开 (0:否, 1:是)
     */
    private Integer isPublic;

    /**
     * 父文件 ID (用于实现文件夹层级结构)
     */
    private Integer parentId;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;

    /**
     * 记录更新时间
     */
    private LocalDateTime updateTime;
}

package com.office.newofficeautomationbackend.mapper;

import com.office.newofficeautomationbackend.entity.File;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 文件管理持久层接口 (MyBatis Mapper)
 * 负责文件元数据在数据库中的增删改查
 */
@Mapper
public interface FileMapper {

    /**
     * 插入新的文件记录
     * @param file 文件实体对象
     * @return 受影响的行数
     */
    @Insert("INSERT INTO oa_file (file_name, original_name, file_path, file_size, file_type, mime_type, " +
            "share_type, uploader_id, download_count, is_public, parent_id, create_time, update_time) " +
            "VALUES (#{fileName}, #{originalName}, #{filePath}, #{fileSize}, #{fileType}, #{mimeType}, " +
            "#{shareType}, #{uploaderId}, #{downloadCount}, #{isPublic}, #{parentId}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(File file);

    /**
     * 分页查询文件列表
     * @param keyword 模糊匹配原始文件名
     * @return 文件列表 (PageHelper 自动处理分页)
     */
    @Select("SELECT * FROM oa_file WHERE original_name LIKE CONCAT('%', #{keyword}, '%') ORDER BY create_time DESC")
    List<File> selectByKeyword(@Param("keyword") String keyword);

    /**
     * 根据父级 ID 分页查询文件和文件夹
     * @param userId 当前用户 ID (视角隔离)
     * @param parentId 父文件夹 ID (根目录为 null)
     * @param keyword 搜索关键字
     * @return 文件列表
     */
    @Select("<script>" +
            "SELECT * FROM oa_file WHERE uploader_id = #{userId} " +
            "<if test='parentId != null'> AND parent_id = #{parentId} </if>" +
            "<if test='parentId == null'> AND parent_id IS NULL </if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND original_name LIKE CONCAT('%', #{keyword}, '%') " +
            "</if>" +
            "ORDER BY (CASE WHEN file_type = 'folder' THEN 0 ELSE 1 END), create_time DESC" +
            "</script>")
    List<File> selectByParentId(@Param("userId") Integer userId, 
                                @Param("parentId") Integer parentId, 
                                @Param("keyword") String keyword);

    /**
     * 根据 ID 获取文件详细信息
     */
    @Select("SELECT * FROM oa_file WHERE id = #{id}")
    File getById(Integer id);

    /**
     * 根据新文件名 获取文件详细信息
     */
    @Select("SELECT * FROM oa_file WHERE file_name = #{fileName}")
    File getByNewFileName(String fileName);

    /**
     * 根据 ID 删除文件记录
     * @param id 文件 ID
     * @return 受影响的行数
     */
    @Delete("DELETE FROM oa_file WHERE id = #{id}")
    int deleteById(Integer id);


    /**
     * 根据新文件名 删除文件记录
     * @param fileName 文件名
     * @return 受影响的行数
     */
    @Delete("DELETE FROM oa_file WHERE file_name = #{fileName}")
    int deleteByNewFileName(String fileName);


    /**
     * 更新文件或文件夹信息
     */
    @Update("UPDATE oa_file SET original_name=#{originalName}, parent_id=#{parentId}, update_time=#{updateTime} WHERE id=#{id}")
    int update(File file);

    /**
     * 增加文件的下载次数
     * @param id 文件 ID
     */
    @Update("UPDATE oa_file SET download_count = download_count + 1 WHERE id = #{id}")
    int updateDownloadCount(Integer id);
}

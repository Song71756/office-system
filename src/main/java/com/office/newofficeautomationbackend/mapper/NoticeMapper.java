package com.office.newofficeautomationbackend.mapper;

import com.office.newofficeautomationbackend.entity.Notice;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 通知公告持久层接口
 * 负责 oa_notice 表的增删改查
 */
@Mapper
public interface NoticeMapper {

    /**
     * 根据关键字分页查询通知列表
     * @param keyword 标题模糊搜索
     * @param status 状态过滤 (可选)
     * @return 通知集合
     */
    @Select("<script>" +
            "SELECT * FROM oa_notice WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND title LIKE CONCAT('%', #{keyword}, '%') " +
            "</if>" +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<Notice> selectPage(@Param("keyword") String keyword, @Param("status") Integer status);

    /**
     * 根据 ID 获取通知详情
     */
    @Select("SELECT * FROM oa_notice WHERE id = #{id}")
    Notice getById(Integer id);

    /**
     * 插入新通知
     */
    @Insert("INSERT INTO oa_notice (title, content, type, status, priority, publisher_id, publish_time, view_count, attachment_name,new_file_name,attachment_path, end_time, create_time, update_time) " +
            "VALUES (#{title}, #{content}, #{type}, #{status}, #{priority}, #{publisherId}, #{publishTime}, #{viewCount}, #{attachmentName},#{newFileName}, #{attachmentPath}, #{endTime}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Notice notice);

    /**
     * 更新通知内容
     */
    @Update("UPDATE oa_notice SET title=#{title}, content=#{content}, type=#{type}, status=#{status}, priority=#{priority}, " +
            "publish_time=#{publishTime}, attachment_name=#{attachmentName},new_file_name=#{newFileName}, attachment_path=#{attachmentPath}, " +
            "end_time=#{endTime}, update_time=#{updateTime} WHERE id=#{id}")
    int update(Notice notice);

    /**
     * 增加阅读量
     */
    @Update("UPDATE oa_notice SET view_count = view_count + 1 WHERE id = #{id}")
    void addViewCount(Integer id);

    /**
     * 删除通知
     */
    @Delete("DELETE FROM oa_notice WHERE id = #{id}")
    int deleteById(Integer id);
}

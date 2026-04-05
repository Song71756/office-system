package com.office.newofficeautomationbackend.mapper;

import com.office.newofficeautomationbackend.entity.Document;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 公文管理持久层接口
 * 负责 oa_document 表的数据交互
 */
@Mapper
public interface DocumentMapper {

    /**
     * 安全分页查询公文列表 (带视角隔离)
     * 逻辑：
     * 1. 所有人可见已通过 (status=2) 的公文
     * 2. 创建者可见自己起草的所有公文
     * 3. 有审批权限的用户可见待审批 (status=1) 的公文
     * @param keyword 标题或编号模糊匹配
     * @param status 状态精准过滤 (可选)
     * @param userId 当前登录用户 ID (用于判断可见性)
     * @param hasApprovePermission 当前用户是否拥有审批权限
     * @return 公文列表
     */
    @Select("<script>" +
            "SELECT * FROM oa_document WHERE ((status = 2) OR (creator_id = #{userId}) " +
            "<if test='hasApprovePermission'>" +
            "OR (status = 1) " +
            "</if>" +
            ") " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (title LIKE CONCAT('%', #{keyword}, '%') OR doc_number LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='status != null'>" +
            "AND status = #{status} " +
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<Document> selectVisiblePage(@Param("keyword") String keyword, 
                                     @Param("status") Integer status, 
                                     @Param("userId") Integer userId,
                                     @Param("hasApprovePermission") boolean hasApprovePermission);

    @Select("SELECT *  FROM oa_document WHERE creator_id = #{userId}")
    List<Document> selectByCurrentUserId(@Param("userId") Integer userId);

    /**
     * 根据 ID 获取公文详情
     */
    @Select("SELECT * FROM oa_document WHERE id = #{id}")
    Document getById(Integer id);

    /**
     * 新增公文 (起草)
     */
    @Insert("INSERT INTO oa_document (doc_number, title, content, type, department_id, file_id, priority, status, creator_id, create_time, update_time) " +
            "VALUES (#{docNumber}, #{title}, #{content}, #{type}, #{departmentId}, #{fileId}, #{priority}, #{status}, #{creatorId}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Document document);

    /**
     * 更新公文内容
     */
    @Update("UPDATE oa_document SET doc_number=#{docNumber}, title=#{title}, content=#{content}, type=#{type}, " +
            "department_id=#{departmentId}, file_id=#{fileId}, priority=#{priority}, status=#{status}, update_time=#{updateTime} WHERE id=#{id}")
    int update(Document document);

    /**
     * 更新公文审批状态
     */
    @Update("UPDATE oa_document SET status=#{status}, approver_id=#{approverId}, approve_comment=#{approveComment}, " +
            "approve_time=#{approveTime}, update_time=NOW() WHERE id=#{id}")
    int updateApproveStatus(@Param("id") Integer id, @Param("status") Integer status, 
                            @Param("approverId") Integer approverId, @Param("approveComment") String approveComment, 
                            @Param("approveTime") java.time.LocalDateTime approveTime);

    /**
     * 删除公文记录
     */
    @Delete("DELETE FROM oa_document WHERE id = #{id}")
    int deleteById(Integer id);

    /**
     * 获取公文总数 (用于生成自动编号)
     * @return 数据库中已有的公文总数
     */
    @Select("SELECT COUNT(*) FROM oa_document")
    int countTotal();
}

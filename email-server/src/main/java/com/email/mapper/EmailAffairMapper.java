package com.email.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.email.entity.EmailAffair;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 邮件事务 Mapper —— 核心查询入口。
 *
 * <p>收件箱/发件箱/草稿箱/已删除/收藏/加密 全部通过 state 字段区分，
 * 复杂 HQL 查询迁移为 MyBatis XML 中的 SQL。</p>
 */
@Mapper
public interface EmailAffairMapper extends BaseMapper<EmailAffair> {

    /** 收件箱 */
    IPage<EmailAffair> findInbox(Page<EmailAffair> page,
                                  @Param("userId") Long userId,
                                  @Param("params") Map<String, Object> params);

    /** 发件箱 */
    IPage<EmailAffair> findSent(Page<EmailAffair> page,
                                 @Param("userId") Long userId,
                                 @Param("params") Map<String, Object> params);

    /** 草稿箱 */
    IPage<EmailAffair> findDraft(Page<EmailAffair> page,
                                  @Param("userId") Long userId,
                                  @Param("params") Map<String, Object> params);

    /** 已删除 */
    IPage<EmailAffair> findDeleted(Page<EmailAffair> page,
                                    @Param("userId") Long userId,
                                    @Param("params") Map<String, Object> params);

    /** 收藏 */
    IPage<EmailAffair> findCollect(Page<EmailAffair> page,
                                    @Param("userId") Long userId,
                                    @Param("params") Map<String, Object> params);

    /** 加密箱 */
    IPage<EmailAffair> findSecret(Page<EmailAffair> page,
                                   @Param("params") Map<String, Object> params);

    /** 自定义文件夹下邮件 */
    IPage<EmailAffair> findByPath(Page<EmailAffair> page,
                                   @Param("userId") Long userId,
                                   @Param("path") String path,
                                   @Param("params") Map<String, Object> params);

    /** 聚合统计数量（替代多次单独的 count 查询） */
    Map<String, Integer> countGroupByState(@Param("userId") Long userId);
}

package com.email.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.email.entity.EmailSummary;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮件主体 Mapper。
 */
@Mapper
public interface EmailSummaryMapper extends BaseMapper<EmailSummary> {

    /** MyBatis-Plus 自带 CRUD，复杂查询在 EmailAffairMapper 中处理 */
}

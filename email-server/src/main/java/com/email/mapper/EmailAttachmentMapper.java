package com.email.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.email.entity.EmailAttachment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmailAttachmentMapper extends BaseMapper<EmailAttachment> {
}

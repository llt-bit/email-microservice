package com.email.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.email.entity.EmailFolder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmailFolderMapper extends BaseMapper<EmailFolder> {

    List<EmailFolder> findByMemberId(@Param("memberId") Long memberId,
                                      @Param("isDelete") Integer isDelete);

    EmailFolder findByMemberIdAndPath(@Param("memberId") Long memberId,
                                       @Param("path") String path);
}

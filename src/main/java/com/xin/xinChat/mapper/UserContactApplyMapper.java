package com.xin.xinChat.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xin.xinChat.model.entity.UserContactApply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author 15712
* @description 针对表【userContactApply(联系人申请)】的数据库操作Mapper
* @createDate 2024-06-20 20:47:02
* @Entity com.xin.xinChat.model.entity.UserContactApply
*/
public interface UserContactApplyMapper extends BaseMapper<UserContactApply> {

    IPage<UserContactApply> selectUserContactApplyWithPage(Page<UserContactApply> page, @Param("receiveUserId") String receiveUserId);

}





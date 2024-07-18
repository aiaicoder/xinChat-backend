package com.xin.xinChat.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xin.xinChat.model.entity.ChatSessionUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 15712
* @description 针对表【ChatSessionUser】的数据库操作Mapper
* @createDate 2024-07-16 20:22:59
* @Entity generator.domain.ChatSessionUser
*/
public interface ChatSessionUserMapper extends BaseMapper<ChatSessionUser> {

    List<ChatSessionUser> selectChatSessionContactList(@Param("userId") String userId);

    void insertOrUpdateBatch(@Param("chatSessionUserList") List<ChatSessionUser> chatSessionUserList);

}





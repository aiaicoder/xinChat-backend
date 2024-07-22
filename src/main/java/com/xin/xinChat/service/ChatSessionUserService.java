package com.xin.xinChat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xin.xinChat.model.entity.ChatSessionUser;


/**
* @author 15712
* @description 针对表【ChatSessionUser】的数据库操作Service
* @createDate 2024-07-16 20:22:59
*/
public interface ChatSessionUserService extends IService<ChatSessionUser> {

    void removeRedundancyInfo(String contactName, String contactId);

}

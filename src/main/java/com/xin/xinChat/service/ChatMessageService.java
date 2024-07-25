package com.xin.xinChat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xin.xinChat.model.dto.Message.MessageSendDTO;
import com.xin.xinChat.model.entity.ChatMessage;


/**
* @author 15712
* @description 针对表【ChatMessage】的数据库操作Service
* @createDate 2024-07-16 20:20:50
*/
public interface ChatMessageService extends IService<ChatMessage> {

    MessageSendDTO saveMessage(ChatMessage chatMessage);

    void saveFile(ChatMessage chatMessage,String userId, Long messageId, String fileUrl);

}

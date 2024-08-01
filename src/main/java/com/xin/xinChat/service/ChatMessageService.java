package com.xin.xinChat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qcloud.cos.model.COSObject;
import com.xin.xinChat.model.dto.Message.MessageSendDTO;
import com.xin.xinChat.model.entity.ChatMessage;
import com.xin.xinChat.model.entity.User;


/**
* @author 15712
* @description 针对表【ChatMessage】的数据库操作Service
* @createDate 2024-07-16 20:20:50
*/
public interface ChatMessageService extends IService<ChatMessage> {

    MessageSendDTO saveMessage(User loginUser,ChatMessage chatMessage);

    void saveFile(ChatMessage chatMessage, Long messageId, String fileUrl,String filePath);

    void checkFileAuth(User loginUser, Long fileId);

    void recallMessage(Long messageId);

    void showRecallMessage(String userId,ChatMessage chatMessage);
}

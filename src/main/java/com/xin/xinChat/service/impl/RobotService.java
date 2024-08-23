package com.xin.xinChat.service.impl;

import com.xin.xinChat.manager.AiManager;
import com.xin.xinChat.model.dto.system.SysSettingDTO;
import com.xin.xinChat.model.entity.ChatMessage;
import com.xin.xinChat.model.entity.User;
import com.xin.xinChat.model.enums.MessageTypeEnum;
import com.xin.xinChat.service.ChatMessageService;
import com.xin.xinChat.utils.SysSettingUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author 15712
 */
@Service
public class RobotService {

    @Resource
    private AiManager aiManager;
    @Resource
    private ChatMessageService chatMessageService;



    @Async
    public void handleRobotReply(User robotUser ,String sendUserId, String messageContent) {
        // 机器人回复的消息
        ChatMessage chatMessageRobot = new ChatMessage();
        chatMessageRobot.setContactId(sendUserId);
        String aiAnswer = aiManager.doChat(sendUserId, messageContent);
        chatMessageRobot.setMessageContent(aiAnswer);
        chatMessageRobot.setMessageType(MessageTypeEnum.CHAT.getType());
        chatMessageService.saveMessage(robotUser, chatMessageRobot);
    }
}

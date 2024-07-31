package com.xin.xinChat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.constant.UserConstant;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.manager.CosManager;
import com.xin.xinChat.mapper.ChatMessageMapper;
import com.xin.xinChat.model.dto.Message.MessageSendDTO;
import com.xin.xinChat.model.dto.system.SysSettingDTO;
import com.xin.xinChat.model.entity.ChatMessage;
import com.xin.xinChat.model.entity.ChatSession;
import com.xin.xinChat.model.entity.User;
import com.xin.xinChat.model.entity.UserContact;
import com.xin.xinChat.model.enums.*;
import com.xin.xinChat.service.ChatMessageService;
import com.xin.xinChat.service.ChatSessionService;
import com.xin.xinChat.service.UserContactService;
import com.xin.xinChat.service.UserService;
import com.xin.xinChat.utils.RedisUtils;
import com.xin.xinChat.utils.StringUtil;
import com.xin.xinChat.utils.SysSettingUtil;
import com.xin.xinChat.websocket.MessageHandler;
import jodd.util.ArraysUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author 15712
 * @description 针对表【ChatMessage】的数据库操作Service实现
 * @createDate 2024-07-16 20:20:50
 */
@Service
@Slf4j
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
        implements ChatMessageService {
    @Resource
    private UserService userService;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private ChatSessionService chatSessionService;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private UserContactService userContactService;

    @Resource
    private SysSettingUtil sysSettingUtil;


    @Override
    public MessageSendDTO saveMessage(ChatMessage chatMessage) {
        User loginUser = userService.getLoginUser();
        String sendUserId = loginUser.getId();
        String contactId = chatMessage.getContactId();
        //判断不是机器人回复
        if (!UserConstant.ROBOT_UID.equals(sendUserId)) {
            List<String> contactList = redisUtils.getContactList(sendUserId);
            if (!contactList.contains(contactId)) {
                UserContactEnum userContactEnum = UserContactEnum.getEnumByPrefix(contactId);
                if (userContactEnum == UserContactEnum.USER) {
                    throw new BusinessException(ErrorCode.NOT_FRIEND_ERROR);
                } else {
                    throw new BusinessException(ErrorCode.NOT_IN_GROUP_ERROR);
                }
            }
        }
        String sessionId = null;
        Integer messageType = chatMessage.getMessageType();
        chatMessage.setSendTime(System.currentTimeMillis());
        UserContactEnum userContactEnum = UserContactEnum.getEnumByPrefix(contactId);
        //设置相应的会话Id
        if (UserContactEnum.USER == userContactEnum) {
            sessionId = StringUtil.getSessionId(new String[]{sendUserId, contactId});
        } else {
            sessionId = StringUtil.getSessionIdGroup(contactId);
        }
        chatMessage.setSessionId(sessionId);
        //判断消息类型
        if (messageType == null ||
                !ArraysUtil.contains(new Integer[]{MessageTypeEnum.CHAT.getType(),
                        MessageTypeEnum.MEDIA_CHAT.getType()}, messageType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请发送正确的消息类型");
        }
        //设置消息状态
        Integer status = MessageTypeEnum.MEDIA_CHAT.getType().equals(messageType) ?
                MessageStatusEnum.SENDING.getStatus() : MessageStatusEnum.SENDED.getStatus();
        chatMessage.setStatus(status);
        //处理消息中的特殊字符防止xss攻击
        String messageContent = StringUtil.htmlEscape(chatMessage.getMessageContent());
        chatMessage.setMessageContent(messageContent);
        //更新会话信息
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        if (UserContactEnum.GROUP == userContactEnum) {
            chatSession.setLastMessage(loginUser.getUserName() + "：" + messageContent);
        }
        chatSession.setLastReceiveTime(System.currentTimeMillis());
        chatSessionService.updateById(chatSession);
        //更新消息表
        chatMessage.setContactType(userContactEnum.getType());
        chatMessage.setSendUserId(sendUserId);
        chatMessage.setSendUserName(loginUser.getUserName());
        this.save(chatMessage);
        //发送消息
        MessageSendDTO messageSendDTO = BeanUtil.copyProperties(chatMessage, MessageSendDTO.class);
        //这里要区分一下是否是发给机器人的
        if (UserConstant.ROBOT_UID.equals(contactId)) {
            SysSettingDTO sysSetting = sysSettingUtil.getSysSetting();
            User robotUser = new User();
            robotUser.setId(sysSetting.getRobotUid());
            robotUser.setUserName(sysSetting.getRobotNickName());
            //机器人回复的消息
            ChatMessage chatMessageRobot = new ChatMessage();
            chatMessageRobot.setContactId(sendUserId);
            //这里可以对接ai实现聊天
            chatMessageRobot.setMessageContent("我只是一个机器人无法和你聊天");
            chatMessageRobot.setMessageType(MessageTypeEnum.CHAT.getType());
            saveMessage(chatMessage);
        } else {
            messageHandler.sendMessage(messageSendDTO);
        }
        return messageSendDTO;


    }

    @Override
    public void saveFile(ChatMessage chatMessage, Long messageId, String fileUrl,String filePath) {
        chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
        chatMessage.setFilePath(filePath);
        UpdateWrapper<ChatMessage> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("status", MessageStatusEnum.SENDING.getStatus());

        update(chatMessage, updateWrapper);
        //发送消息
        MessageSendDTO messageSendDTO = new MessageSendDTO();
        messageSendDTO.setStatus(MessageStatusEnum.SENDED.getStatus());
        messageSendDTO.setMessageId(messageId);
        messageSendDTO.setMessageType(MessageTypeEnum.FILE_UPLOAD.getType());
        messageSendDTO.setContactId(chatMessage.getContactId());
        messageSendDTO.setMessageContent(fileUrl);
        messageHandler.sendMessage(messageSendDTO);
    }

    @Override
    public void checkFileAuth(User loginUser,Long messageId) {
        ChatMessage chatMessage = getById(messageId);
        String userId = loginUser.getId();
        if (chatMessage == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        String contactId = chatMessage.getContactId();
        UserContactEnum userContactEnum = UserContactEnum.getEnumByPrefix(contactId);
        //判断当前用户是否是对方的消息发送方
        if (UserContactEnum.USER == userContactEnum && !userId.equals(contactId)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //如果是群聊消息判断当前用户是否在群中
        if (UserContactEnum.GROUP == userContactEnum){
            QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
            userContactQueryWrapper.eq("status", UserContactStatusEnum.FRIEND.getStatus());
            userContactQueryWrapper.eq("contactId",contactId);
            userContactQueryWrapper.eq("contactType",UserContactEnum.GROUP.getType());
            userContactQueryWrapper.eq("userId",userId);
            long count = userContactService.count(userContactQueryWrapper);
            if (count == 0){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }

    @Override
    public void recallMessage(Long messageId) {
        ChatMessage chatMessage = getById(messageId);
        if (chatMessage == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = userService.getLoginUser();
        String userId = loginUser.getId();
        //消息撤回权限的校验
        checkRecallMessage(loginUser,chatMessage);
        //消息的撤回
        chatMessage.setStatus(MessageStatusEnum.RECALLED.getStatus());
        chatMessage.setRecallId(userId);
        updateById(chatMessage);
        //发送消息通知
        MessageSendDTO messageSendDTO = new MessageSendDTO();
        messageSendDTO.setExtendData(userService.getUserVO(loginUser));
        messageSendDTO.setSenderUserId(userId);
        messageSendDTO.setContactId(chatMessage.getContactId());
        messageSendDTO.setMessageId(messageId);
        messageSendDTO.setMessageType(MessageTypeEnum.RECALL_MESSAGE.getType());
        messageSendDTO.setSendTime(System.currentTimeMillis());
        String messageContent = String.format(MessageTypeEnum.RECALL_MESSAGE.getInitMessage(),chatMessage.getSendUserName());
        if (!userId.equals(chatMessage.getSendUserId())){
             messageContent = String.format(MessageTypeEnum.RECALL_MESSAGE.getInitMessage(),"管理员\t" + loginUser.getUserName()+"撤回了一条群成员消息");
        }
        messageSendDTO.setMessageContent(messageContent);
        messageHandler.sendMessage(messageSendDTO);
    }

    private void checkRecallMessage(User loginUser,ChatMessage chatMessage){
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(chatMessage.getMessageType());
        if (MessageTypeEnum.RECALL_MESSAGE == messageTypeEnum){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息已被撤回");
        }
        if (!loginUser.getId().equals(chatMessage.getSendUserId()) && !loginUser.getUserRole().equals(UserRoleEnum.ADMIN.getValue())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限");
        }
        Date sendDate = new Date(chatMessage.getSendTime());
        Date now = new Date();
        long between = DateUtil.between(sendDate, now, DateUnit.MINUTE);
        if (between > 2){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超过两分钟的消息，不能撤回哦");
        }
    }


    @Override
    public void showRecallMessage(String userId,ChatMessage chatMessage) {
        String sendUserId = chatMessage.getSendUserId();
        String recallId = chatMessage.getRecallId();
        String contactId = chatMessage.getContactId();
        UserContactEnum contactEnum = UserContactEnum.getEnumByPrefix(contactId);
        User recallUser = userService.getById(recallId);
        if (!recallId.equals(sendUserId) && contactEnum == UserContactEnum.GROUP){
            if (recallUser != null){
                chatMessage.setMessageContent(String.format(MessageTypeEnum.RECALL_MESSAGE.getInitMessage(),"管理员\t" + recallUser.getUserName()));
            }
        }else if (recallId.equals(sendUserId) && !sendUserId.equals(userId)){
            chatMessage.setMessageContent(String.format(MessageTypeEnum.RECALL_MESSAGE.getInitMessage(),recallUser.getUserName()));
        }else if (recallId.equals(userId)){
            chatMessage.setMessageContent(String.format(MessageTypeEnum.RECALL_MESSAGE.getInitMessage(),"你"));
        }
    }
}





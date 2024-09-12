package com.xin.xinChat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.mapper.ChatSessionUserMapper;
import com.xin.xinChat.model.dto.Message.MessageSendDTO;
import com.xin.xinChat.model.entity.ChatSessionUser;
import com.xin.xinChat.model.entity.UserContact;
import com.xin.xinChat.model.enums.MessageTypeEnum;
import com.xin.xinChat.model.enums.UserContactEnum;
import com.xin.xinChat.model.enums.UserContactStatusEnum;
import com.xin.xinChat.service.ChatSessionUserService;

import com.xin.xinChat.service.UserContactService;
import com.xin.xinChat.websocket.MessageHandler;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
* @author 15712
* @description 针对表【ChatSessionUser】的数据库操作Service实现
* @createDate 2024-07-16 20:22:59
*/
@Service
public class ChatSessionUserServiceImpl extends ServiceImpl<ChatSessionUserMapper, ChatSessionUser>
    implements ChatSessionUserService {

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private UserContactService userContactService;

    @Override
    public void removeRedundancyInfo(String userId,String contactName, String contactId) {
        UserContactEnum enumByPrefix = UserContactEnum.getEnumByPrefix(contactId);
        if (enumByPrefix == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数错误");
        }
        //更新会话表
        ChatSessionUser upChatSessionUser = new ChatSessionUser();
        upChatSessionUser.setContactName(contactName);
        upChatSessionUser.setContactId(contactId);
        upChatSessionUser.setUserId(userId);
        this.updateById(upChatSessionUser);
        if (enumByPrefix == UserContactEnum.GROUP){
            //发送更新消息群消息
            MessageSendDTO messageSendDTO = new MessageSendDTO();
            messageSendDTO.setContactType(enumByPrefix.getType());
            messageSendDTO.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
            messageSendDTO.setExtendData(contactName);
            messageSendDTO.setContactId(contactId);
            //发送消息
            messageHandler.sendMessage(messageSendDTO);
        }else{
            QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
            userContactQueryWrapper.eq("contactId",contactId);
            userContactQueryWrapper.eq("contactType",UserContactEnum.USER.getType());
            userContactQueryWrapper.eq("status", UserContactStatusEnum.FRIEND.getStatus());
            List<UserContact> userContactList = userContactService.list(userContactQueryWrapper);
            for (UserContact userContact : userContactList) {
                MessageSendDTO messageSendDTO = new MessageSendDTO();
                messageSendDTO.setContactType(enumByPrefix.getType());
                messageSendDTO.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
                messageSendDTO.setContactId(userContact.getUserId());
                messageSendDTO.setExtendData(contactName);
                messageSendDTO.setSendUserId(contactId);
                messageSendDTO.setSendUserName(contactName);
                messageHandler.sendMessage(messageSendDTO);
            }

        }

    }
}





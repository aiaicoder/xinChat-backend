package com.xin.xinChat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.config.AppConfig;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.mapper.GroupInfoMapper;
import com.xin.xinChat.mapper.UserMapper;
import com.xin.xinChat.model.dto.Message.MessageSendDTO;
import com.xin.xinChat.model.dto.system.SysSettingDTO;
import com.xin.xinChat.model.entity.*;
import com.xin.xinChat.model.enums.*;
import com.xin.xinChat.service.*;
import com.xin.xinChat.utils.RedisUtils;
import com.xin.xinChat.utils.StringUtil;
import com.xin.xinChat.utils.SysSettingUtil;
import com.xin.xinChat.websocket.ChannelContextUtils;
import com.xin.xinChat.websocket.MessageHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author 15712
 * @description 针对表【groupInfo】的数据库操作Service实现
 * @createDate 2024-06-20 20:45:56
 */
@Service
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo>
        implements GroupInfoService {

    @Resource
    private UserContactService userContactService;

    @Resource
    private SysSettingUtil sysSettingUtil;

    @Resource
    private ChatSessionService chatSessionService;

    @Resource
    private ChatSessionUserService chatSessionUserService;

    @Resource
    private ChannelContextUtils channelContextUtils;

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private UserContactApplyService userContactApplyService;

    @Resource
    private UserMapper userMapper;

    @Resource
    @Lazy
    private GroupInfoService groupInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveGroup(GroupInfo groupInfo) {
        String groupId = groupInfo.getGroupId();
        String groupOwnerId = groupInfo.getGroupOwnerId();
        String groupAvatar = groupInfo.getGroupAvatar();
        //如果id为空表示新增
        if (StringUtils.isBlank(groupId)) {
            String groupIdStr = StringUtil.getGroupId();
            groupInfo.setGroupId(groupIdStr);
            QueryWrapper<GroupInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("groupOwnerId", groupOwnerId);
            queryWrapper.eq("status",0);
            long count = this.count(queryWrapper);
            SysSettingDTO sysSetting = sysSettingUtil.getSysSetting();
            if (count > sysSetting.getMaxGroupCount()) {
                throw new BusinessException(ErrorCode.MAX_GROUP_COUNT_ERROR, "最多支持创建" + sysSetting.getMaxGroupCount() + "个群聊");
            }
            if (StringUtils.isBlank(groupAvatar)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "群聊头像不能为空");
            }
            boolean save = this.save(groupInfo);
            //将群聊设置为联系人
            UserContact userContact = new UserContact();
            //设置联系id
            userContact.setContactId(groupIdStr);
            //设置好友状态
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            //设置联系类型为群聊
            userContact.setContactType(UserContactEnum.GROUP.getType());
            userContact.setUserId(groupOwnerId);
            boolean savaUserContact = userContactService.save(userContact);
            //创建会话  
            String sessionId = StringUtil.getSessionIdGroup(groupIdStr);
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatSession.setLastReceiveTime(System.currentTimeMillis());
            if (chatSessionService.getById(sessionId) == null){
                chatSessionService.save(chatSession);
            }else {
                chatSessionService.updateById(chatSession);
            }
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setSessionId(sessionId);
            chatSessionUser.setUserId(groupInfo.getGroupOwnerId());
            chatSessionUser.setContactName(groupInfo.getGroupName());
            chatSessionUser.setContactId(groupInfo.getGroupId());
            chatSessionUser.setAvatar(groupAvatar);
            chatSessionUserService.save(chatSessionUser);
            //创建消息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(MessageTypeEnum.GROUP_CREATE.getType());
            chatMessage.setMessageContent(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatMessage.setContactId(groupInfo.getGroupId());
            chatMessage.setContactType(UserContactEnum.GROUP.getType());
            chatMessage.setSendTime(System.currentTimeMillis());
            chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
            chatMessageService.save(chatMessage);
            //将群组添加到联系人缓存
            redisUtils.addUserContact(groupInfo.getGroupOwnerId(), groupInfo.getGroupId(), appConfig.tokenTimeout, TimeUnit.SECONDS);
            //添加群聊天，将联系人通道添加到群组通道
            channelContextUtils.addGroupContext(groupInfo.getGroupId(),groupInfo.getGroupOwnerId());
            chatSessionUser.setLastMessage(MessageTypeEnum.GROUP_CREATE.getInitMessage());
            chatSessionUser.setLastReceiveTime(System.currentTimeMillis());
            chatSessionUser.setMemberCount(1);
            //准备发送消息
            MessageSendDTO messageSendDTO = BeanUtil.copyProperties(chatMessage, MessageSendDTO.class);
            messageSendDTO.setExtendData(chatSessionUser);
            messageSendDTO.setLastMessage(chatSessionUser.getLastMessage());
            messageHandler.sendMessage(messageSendDTO);
            if (save && savaUserContact) {
                return groupIdStr;
            }
        }else {
            GroupInfo oldGroupInfo = this.getById(groupId);
            if (!oldGroupInfo.getGroupOwnerId().equals(groupInfo.getGroupOwnerId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            GroupInfo newGroupInfo = new GroupInfo();
            BeanUtil.copyProperties(groupInfo,newGroupInfo);
            boolean b = this.updateById(newGroupInfo);
            //更新相关表冗余信息
            String updateContactName = null;
            if (!oldGroupInfo.getGroupName().equals(groupInfo.getGroupName())){
                updateContactName = groupInfo.getGroupName();
            }
            if (updateContactName == null){
                return "-1";
            }
            chatSessionUserService.removeRedundancyInfo(updateContactName,groupId);
            if (b){
                return groupId;
            }
        }
        return null;
    }

    /**
     * 管理员和普通的群聊可以解散
     * @param groupOwnerId
     * @param groupId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dismissGroup(String groupOwnerId, String groupId) {
        GroupInfo dbInfo = getById(groupId);
        if (!dbInfo.getGroupOwnerId().equals(groupOwnerId)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //解散群聊
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId(groupId);
        groupInfo.setStatus(GroupInfoEnum.DISMISSAL.getStatus());
        this.updateById(groupInfo);
        //更新联系人信息
        UpdateWrapper<UserContact> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("contactId",groupId);
        updateWrapper.eq("contactType",UserContactEnum.GROUP.getType());
        updateWrapper.set("status",UserContactStatusEnum.DEL.getStatus());
        userContactService.update(updateWrapper);
        //移除相关 群聊的联系人缓存
        QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
        userContactQueryWrapper.eq("contactId",groupId);
        userContactQueryWrapper.eq("contactType",UserContactEnum.GROUP.getType());
        List<UserContact> userContactList = userContactService.list(userContactQueryWrapper);
        //删除缓存中的联系人信息
        userContactList.forEach(userContact -> {
            redisUtils.delUserContactInfo(userContact.getUserId(),userContact.getContactId());
        });
        //发消息 1.更新会话消息，2.记录群消息，3.发送群解散通知
        //当前时间
        long currentTimeMillis = System.currentTimeMillis();
        //更新会话信息
        ChatSession chatSession = new ChatSession();
        String sessionIdGroup = StringUtil.getSessionIdGroup(groupId);
        String messageContent = MessageTypeEnum.DISSOLUTION_GROUP.getInitMessage();
        chatSession.setSessionId(sessionIdGroup);
        chatSession.setLastMessage(messageContent);
        chatSession.setLastReceiveTime(currentTimeMillis);
        chatSessionService.updateById(chatSession);
        //消息列表
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionIdGroup);
        chatMessage.setMessageType(MessageTypeEnum.DISSOLUTION_GROUP.getType());
        chatMessage.setMessageContent(messageContent);
        chatMessage.setContactId(groupId);
        chatMessage.setContactType(UserContactEnum.GROUP.getType());
        chatMessage.setSendTime(currentTimeMillis);
        chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
        //插入消息
        chatMessageService.save(chatMessage);
        MessageSendDTO messageSendDTO = BeanUtil.copyProperties(chatMessage, MessageSendDTO.class);
        //发送通知
        messageHandler.sendMessage(messageSendDTO);
    }

    @Override
    public void addOrRemoveGroupMember(User loginUser, String groupId, String selectContactIds, Integer opType) {
        GroupInfo groupInfo = getById(groupId);
        if (groupInfo == null || groupInfo.getStatus().equals(GroupInfoEnum.DISMISSAL.getStatus())){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (!groupInfo.getGroupOwnerId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        String[] contactIdList= selectContactIds.split(",");
        for (String contactId : contactIdList) {
            if (GroupOpEnum.REMOVE.getType().equals(opType)){
                //移除群聊
                //自己引入自己保证事务生效
                groupInfoService.leaveGroup(contactId,groupId,MessageTypeEnum.REMOVE_GROUP);
            }else {
                userContactApplyService.addContact(contactId,null,groupId,UserContactEnum.GROUP.getType(),null  );
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum) {
        GroupInfo groupInfo = getById(groupId);
        if (groupInfo == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //如果自己是群主不能退出    z
        if (userId.equals(groupInfo.getGroupOwnerId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        QueryWrapper<UserContact> removeContactQueryWrapper =new QueryWrapper<>();
        removeContactQueryWrapper.eq("contactId",groupId);
        removeContactQueryWrapper.eq("userId",userId);
        //退出群移除记录
        boolean remove =userContactService.remove(removeContactQueryWrapper);
        if (!remove){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"退出失败请重试");
        }
        User user = userMapper.selectById(userId);
        String sessionId = StringUtil.getSessionIdGroup(groupId);
        String messageContent = String.format(messageTypeEnum.getInitMessage(),user.getUserName());
        long currentTimeMillis = System.currentTimeMillis();
        //更新会话
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastMessage(messageContent);
        chatSession.setLastReceiveTime(currentTimeMillis);
        chatSessionService.updateById(chatSession);
        //更新消息列表
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageType(messageTypeEnum.getType());
        chatMessage.setMessageContent(messageContent);
        chatMessage.setContactId(groupId);
        chatMessage.setContactType(UserContactEnum.GROUP.getType());
        chatMessage.setSendTime(currentTimeMillis);
        chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
        chatMessageService.save(chatMessage);
        //查询群人数
        QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
        userContactQueryWrapper.eq("contactId",groupId);
        userContactQueryWrapper.eq("status",UserContactStatusEnum.FRIEND.getStatus());
        long count = userContactService.count(userContactQueryWrapper);
        //发送消息
        MessageSendDTO messageSendDTO = BeanUtil.copyProperties(chatMessage, MessageSendDTO.class);
        messageSendDTO.setExtendData(userId);
        messageSendDTO.setMemberCount(count);
        //
        //从联系人列表中移除消息
        redisUtils.delUserContactInfo(userId,groupId);
        messageHandler.sendMessage(messageSendDTO);
    }

    @Override
    public void sysAddGroup(String userId, String groupId) {
        userContactApplyService.addContact(userId,null,groupId,UserContactEnum.GROUP.getType(),null);
    }


}





package com.xin.xinChat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xin.xinChat.common.ErrorCode;
import com.xin.xinChat.config.AppConfig;
import com.xin.xinChat.constant.UserConstant;
import com.xin.xinChat.exception.BusinessException;
import com.xin.xinChat.mapper.ChatSessionUserMapper;
import com.xin.xinChat.mapper.UserContactApplyMapper;
import com.xin.xinChat.model.dto.Message.MessageSendDTO;
import com.xin.xinChat.model.dto.system.SysSettingDTO;
import com.xin.xinChat.model.entity.*;
import com.xin.xinChat.model.enums.*;
import com.xin.xinChat.model.vo.UserSearchVo;
import com.xin.xinChat.service.*;
import com.xin.xinChat.utils.RedisUtils;
import com.xin.xinChat.utils.StringUtil;
import com.xin.xinChat.utils.SysSettingUtil;
import com.xin.xinChat.websocket.ChannelContextUtils;
import com.xin.xinChat.websocket.MessageHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author 15712
 * @description 针对表【userContactApply(联系人申请)】的数据库操作Service实现
 * @createDate 2024-06-20 20:47:02
 */
@Service
public class UsercontactapplyServiceImpl extends ServiceImpl<UserContactApplyMapper, UserContactApply>
        implements UserContactApplyService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private GroupInfoService groupInfoService;

    @Resource
    private UserContactService userContactService;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private AppConfig appConfig;

    @Resource
    private ChatSessionService chatSessionService;

    @Resource
    private ChatSessionUserMapper chatSessionUserMapper;

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    @Lazy
    private ChannelContextUtils channelContextUtils;

    @Resource
    private SysSettingUtil sysSettingUtil;


    @Override
    public UserSearchVo search(String userId, String contactId) {
        UserContactEnum enumByPrefix = UserContactEnum.getEnumByPrefix(contactId);
        if (enumByPrefix == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "搜索类型错误");
        }
        UserSearchVo userSearchVo = new UserSearchVo();
        switch (enumByPrefix) {
            case USER:
                User user = userService.getById(contactId);
                if (user == null) {
                    return null;
                }
                userSearchVo.setNickName(user.getUserName());
                userSearchVo.setSex(user.getSex() == 1 ? "男" : "女");
                userSearchVo.setAreaName(user.getAreaName());
                System.out.println(userSearchVo);
                break;
            case GROUP:
                GroupInfo groupInfo = groupInfoService.getById(contactId);
                if (groupInfo == null || groupInfo.getStatus().equals(GroupInfoEnum.DISMISSAL.getStatus())) {
                    return null;
                }
                userSearchVo.setNickName(groupInfo.getGroupName());
                break;
        }
        userSearchVo.setContactId(contactId);
        //设置联系人或者群组类型
        userSearchVo.setContactType(enumByPrefix.toString());
        //如果是搜索自己那么就直接设置为朋友
        if (userId.equals(contactId)) {
            userSearchVo.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            return userSearchVo;
        }
        //查询是否是好友
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("contactId", contactId);
        UserContact userContact = userContactService.getOne(queryWrapper);
        userSearchVo.setStatus(userContact == null ? null : userContact.getStatus());
        return userSearchVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer applyAdd(String applyInfo, String contactId) {
        User loginUser = userService.getLoginUser();
        if (StringUtils.isBlank(applyInfo)) {
            //默认申请信息
            applyInfo = String.format(UserConstant.DEFAULT_APPLY_INFO, loginUser.getUserName());
        }
        UserContactEnum typeEnum = UserContactEnum.getEnumByPrefix(contactId);
        if (typeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        //申请人id
        String userApplyId = loginUser.getId();
        //接受者id
        String receiveId = contactId;

        //是直接添加还是需要审核
        Integer joinType = null;

        //申请时间
        Long applyTime = System.currentTimeMillis();
        QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userApplyId);
        queryWrapper.eq("contactId", contactId);
        UserContact userContact = userContactService.getOne(queryWrapper);
        if (userContact != null && ArrayUtils.contains(
                new Integer[]{UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus(),
                        UserContactStatusEnum.BLACKLIST_BE.getStatus()},
                userContact.getStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "你已被拉黑，暂时无法添加");
        }
        if (UserContactEnum.GROUP == typeEnum) {
            GroupInfo groupInfo = groupInfoService.getById(contactId);
            if (groupInfo == null || groupInfo.getStatus().equals(GroupInfoEnum.DISMISSAL.getStatus())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "群聊不存在或已解散");
            }
            receiveId = groupInfo.getGroupOwnerId();
            joinType = groupInfo.getJoinType();
        } else {
            //对方信息,如果对方的添加好友是可以直接通过就直接进行添加
            User user = userService.getById(contactId);
            if (user == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            }
            joinType = user.getJoinType();
            if (JoinTypeEnum.JOIN.getType().equals(joinType)) {

                //todo 添加联系人
                return joinType;
            }
        }
        //查询申请记录是否存在
        QueryWrapper<UserContactApply> userContactApplyQueryWrapper = new QueryWrapper<>();
        userContactApplyQueryWrapper.eq("applyUserId", userApplyId);
        userContactApplyQueryWrapper.eq("receiveUserId", receiveId);
        userContactApplyQueryWrapper.eq("contactId", contactId);
        UserContactApply contactApply = getOne(userContactApplyQueryWrapper);
        if (contactApply == null) {
            UserContactApply userContactApply = new UserContactApply();
            userContactApply.setApplyUserId(userApplyId);
            userContactApply.setReceiveUserId(receiveId);
            userContactApply.setApplyInfo(applyInfo);
            userContactApply.setContactType(typeEnum.getType());
            userContactApply.setLastApplyTime(applyTime);
            userContactApply.setContactId(contactId);
            userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            save(userContactApply);
        } else {
            //直接更新即可
            UserContactApply userContactApply = new UserContactApply();
            userContactApply.setApplyInfo(applyInfo);
            userContactApply.setLastApplyTime(applyTime);
            userContactApply.setApplyId(contactApply.getApplyId());
            userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
            boolean b = updateById(userContactApply);
            if (!b) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "添加失败");
            }
        }
        if (contactApply == null || contactApply.getStatus().equals(UserContactApplyStatusEnum.INIT.getStatus())) {
            //发送ws消息，让用户知晓
            MessageSendDTO messageSendDTO = new MessageSendDTO();
            messageSendDTO.setMessageType(MessageTypeEnum.CONTACT_APPLY.getType());
            messageSendDTO.setContactId(receiveId);
            messageSendDTO.setMessageContent(applyInfo);
            messageHandler.sendMessage(messageSendDTO);
        }
        return joinType;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean dealWithApply(Integer applyId, Integer status) {
        User loginUser = userService.getLoginUser();
        if (applyId == null || status == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        UserContactApplyStatusEnum typeEnum = UserContactApplyStatusEnum.getEnumByStatus(status);
        if (typeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        UserContactApply contactApply = getById(applyId);
        if (contactApply == null || status.equals(UserContactApplyStatusEnum.INIT.getStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "好友申请不存在或申请状态有误");
        }
        if (!contactApply.getReceiveUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "处理失败");
        }
        //更新申请状态
        UserContactApply updateContactApply = new UserContactApply();
        updateContactApply.setApplyId(applyId);
        updateContactApply.setStatus(status);
        updateContactApply.setLastApplyTime(System.currentTimeMillis());
        UpdateWrapper<UserContactApply> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("status", UserContactApplyStatusEnum.INIT.getStatus());
        boolean update = this.update(updateContactApply, updateWrapper);
        if (!update) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "处理失败");
        }
        if (status.equals(UserContactApplyStatusEnum.AGREE.getStatus())) {
            try{
                addContact(contactApply.getApplyUserId(),
                        contactApply.getReceiveUserId(),
                        contactApply.getContactId(),
                        contactApply.getContactType(),
                        contactApply.getApplyInfo());
            }catch (Exception e){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "添加失败");
            }
            return true;
        }
        if (UserContactApplyStatusEnum.BLACKLIST == typeEnum) {
            //被拉黑后更新userContact表先查询是否有对应记录
            String applyUserId = contactApply.getApplyUserId();
            String contactId = contactApply.getContactId();
            Integer contactType = contactApply.getContactType();
            QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
            userContactQueryWrapper.eq("userId", applyUserId);
            userContactQueryWrapper.eq("contactId", contactId);
            UserContact userContact = new UserContact();
            //有就执行更新操作
            if (userContactService.getOne(userContactQueryWrapper) != null) {
                //更新
                userContact.setUserId(applyUserId);
                userContact.setContactId(contactId);
                userContact.setContactType(contactType);
                userContact.setStatus(UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus());
                userContactService.updateById(userContact);
            } else {
                //添加
                userContact.setUserId(applyUserId);
                userContact.setContactId(contactId);
                userContact.setContactType(contactType);
                userContact.setStatus(UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus());
                userContactService.save(userContact);
            }
            return true;
        }
        return false;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType,String applyInfo) {
        //如果是添加的是群，那么要先判断群是否已满
        if (UserContactEnum.GROUP.getType().equals(contactType)) {
            QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("contactId", contactId);
            queryWrapper.eq("status", UserContactStatusEnum.FRIEND.getStatus());
            long count = userContactService.count(queryWrapper);
            //获取后台默认系统设置
            SysSettingDTO sysSetting = sysSettingUtil.getSysSetting();
            if (count >= sysSetting.getMaxGroupMemberCount()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "群已满");
            }
        }
        //同意双方都添加好友
        List<UserContact> userContactList = new ArrayList<>();
        //添加人添加好友
        UserContact userContact = new UserContact();
        userContact.setUserId(applyUserId);
        userContact.setContactId(contactId);
        userContact.setContactType(contactType);
        userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        userContactList.add(userContact);
        //如果加入的不是群聊，而是申请人，那么被申请人也要同时添加上好友，群聊则不需要
        if (UserContactEnum.USER.getType().equals(contactType)){
            UserContact userContactReceive = new UserContact();
            userContactReceive.setUserId(receiveUserId);
            userContactReceive.setContactId(applyUserId);
            userContactReceive.setContactType(contactType);
            userContactReceive.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContactList.add(userContactReceive);
        }
        //批量插入
        userContactService.saveBatch(userContactList);
        //如果是好友，接受人也添加申请人为好友 添加缓存
        if (contactType.equals(UserContactEnum.USER.getType())){
            redisUtils.addUserContact(receiveUserId, applyUserId, appConfig.getTokenTimeout(), TimeUnit.SECONDS);
        }
        redisUtils.addUserContact(applyUserId, receiveUserId, appConfig.getTokenTimeout(), TimeUnit.SECONDS);
        //创建会话 发送消息
        String sessionId = null;
        if (UserContactEnum.USER.getType().equals(contactType)){
            sessionId = StringUtil.getSessionId(new String[]{applyUserId, receiveUserId});
        }else {
            sessionId = StringUtil.getSessionIdGroup(applyUserId);
        }
        List<ChatSessionUser> chatSessionUserList = new ArrayList<>();
        if (UserContactEnum.USER.getType().equals(contactType)){
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastMessage(applyInfo);
            chatSession.setLastReceiveTime(System.currentTimeMillis());
            if (chatSessionService.getById(sessionId) != null){
                chatSessionService.updateById(chatSession);
            }else {
                chatSessionService.save(chatSession);
            }
            //申请人会话
            ChatSessionUser applySessionUser = new ChatSessionUser();
            User contactUser = userService.getById(contactId);
            applySessionUser.setSessionId(sessionId);
            applySessionUser.setUserId(applyUserId);
            applySessionUser.setContactId(contactId);
            applySessionUser.setContactName(contactUser.getUserName());
            //添加进入会话列表
            chatSessionUserList.add(applySessionUser);
            //接收人的会话Id
            User applyuser = userService.getById(applyUserId);
            ChatSessionUser contactSessionUser = new ChatSessionUser();
            contactSessionUser.setSessionId(sessionId);
            contactSessionUser.setUserId(receiveUserId);
            contactSessionUser.setContactId(applyUserId);
            contactSessionUser.setContactName(applyuser.getUserName());
            chatSessionUserList.add(contactSessionUser);
            chatSessionUserMapper.insertOrUpdateBatch(chatSessionUserList);
            //记录消息表
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
            chatMessage.setMessageContent(applyInfo);
            chatMessage.setSendUserId(applyUserId);
            chatMessage.setSendUserName(applyuser.getUserName());
            chatMessage.setContactId(contactId);
            chatMessage.setContactType(UserContactEnum.USER.getType());
            chatMessage.setSendTime(System.currentTimeMillis());
            chatMessageService.save(chatMessage);
            //准备发送消息
            MessageSendDTO messageSendDTO = new MessageSendDTO();
            BeanUtil.copyProperties(chatMessage, messageSendDTO,true);
            //发给接受还有申请的人
            messageHandler.sendMessage(messageSendDTO);
            //发给申请人，发送人就是接受人，联系人就是申请人
            messageSendDTO.setContactId(applyUserId);
            messageSendDTO.setExtendData(contactUser);
            messageSendDTO.setMessageType(MessageTypeEnum.ADD_FRIEND_SELF.getType());
            //接收人给申请人发送消息，本质就是自己给自己发
            messageHandler.sendMessage(messageSendDTO);
        }else {
            //加入群组
            GroupInfo groupInfo = groupInfoService.getById(contactId);
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setSessionId(sessionId);
            chatSessionUser.setUserId(applyUserId);
            chatSessionUser.setContactId(contactId);
            chatSessionUser.setContactName(groupInfo.getGroupName());
            chatSessionUserMapper.insert(chatSessionUser);
            User applyUser = userService.getById(applyUserId);
            String sendMessage = String.format(MessageTypeEnum.ADD_GROUP.getInitMessage(), applyUser.getUserName());
            //创建会话
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastMessage(sendMessage);
            chatSession.setLastReceiveTime(System.currentTimeMillis());
            if (chatSessionService.getById(sessionId) != null){
                chatSessionService.updateById(chatSession);
            }else {
                chatSessionService.save(chatSession);
            }
            //保存消息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageType(MessageTypeEnum.ADD_GROUP.getType());
            chatMessage.setMessageContent(sendMessage);
            chatMessage.setSendTime(System.currentTimeMillis());
            chatMessage.setContactId(contactId);
            chatMessage.setContactType(UserContactEnum.GROUP.getType());
            chatMessage.setStatus(MessageStatusEnum.SENDED.getStatus());
            chatMessageService.save(chatMessage);
            //添加进入联系人列表缓存
            redisUtils.addUserContact(applyUserId,contactId,appConfig.tokenTimeout,TimeUnit.SECONDS);
            //将用户添加到群聊通道
            channelContextUtils.addGroupContext(groupInfo.getGroupId(),applyUserId);
            //发送消息
            MessageSendDTO messageSendDTO = BeanUtil.copyProperties(chatMessage, MessageSendDTO.class);
            messageSendDTO.setContactId(contactId);
            //查看群人数
            QueryWrapper<UserContact> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("contactId",contactId);
            queryWrapper.eq("status",UserContactStatusEnum.FRIEND.getStatus());
            long count = userContactService.count(queryWrapper);
            //设置群人数
            messageSendDTO.setMemberCount(count);
            messageSendDTO.setContactName(groupInfo.getGroupName());
            messageHandler.sendMessage(messageSendDTO);
        }
    }
}





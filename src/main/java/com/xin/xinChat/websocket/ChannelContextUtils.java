package com.xin.xinChat.websocket;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.qcloud.cos.model.ciModel.auditing.UserInfo;
import com.xin.xinChat.mapper.ChatSessionUserMapper;
import com.xin.xinChat.model.dto.Message.MessageSendDTO;
import com.xin.xinChat.model.entity.*;
import com.xin.xinChat.model.enums.MessageTypeEnum;
import com.xin.xinChat.model.enums.UserContactEnum;
import com.xin.xinChat.service.ChatMessageService;
import com.xin.xinChat.service.ChatSessionService;
import com.xin.xinChat.service.UserContactApplyService;
import com.xin.xinChat.service.UserService;
import com.xin.xinChat.utils.RedisUtils;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.xin.xinChat.constant.RedisKeyConstant.REDIS_HEART_BEAT_TIME;
import static com.xin.xinChat.constant.UserConstant.THREE_DAYS_MILLIS;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/15 19:51
 */
@Component
@Slf4j
public class ChannelContextUtils {

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private UserService userService;


    @Resource
    private ChatSessionUserMapper chatSessionUserMapper;

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private UserContactApplyService userContactApplyService;


    //channel的本地缓存
    private static final Cache<String, Channel> USER_CHANNEL_CACHE = Caffeine.newBuilder().
            maximumSize(10000).build();
    private static final Cache<String, ChannelGroup> GROUP_CHANNEL_CACHE = Caffeine.newBuilder().
            maximumSize(10000).
            expireAfterWrite(Duration.ofSeconds(REDIS_HEART_BEAT_TIME)).build();

    /**
     * 准备好要发送的消息
     * @param userId
     * @param channel
     */
    public void addContext(String userId, Channel channel) {
        String channelId = channel.id().toString();
        log.info("userId:{},channelId:{}", userId, channelId);
        AttributeKey attributeKey = null;
        if (!AttributeKey.exists("channelId")) {
            attributeKey = AttributeKey.newInstance(channelId);
        } else {
            attributeKey = AttributeKey.valueOf(channelId);
        }
        channel.attr(attributeKey).set(userId);
        List<String> contactList = redisUtils.getContactList(userId);
        //遍历id，判断是群id还是用户id，如果是群id，则加入群组‘；如果是用户id，则加入用户id对应的群组
        contactList.forEach(contactId -> {
            if (contactId.startsWith(UserContactEnum.GROUP.getPrefix())) {
                addGroupContext(contactId, userId);
            }
        });
        //将chanel放到缓存中管理起来
        USER_CHANNEL_CACHE.put(userId,channel);
        //发送心跳
        redisUtils.setHeartBeatTime(userId);
        //更新最后的上线时间
        User updataUser = new User();
        updataUser.setLastLoginTime(new Date());
        updataUser.setId(userId);
        userService.updateById(updataUser);
        //给用户发消息,只查询三天以前的消息记录
        User userInfo = userService.getById(userId);
        Long sourceLastOffTime = userInfo.getLastOffTime();
        Long lastOffTime = sourceLastOffTime;
        //判断是否三天都没有上线
        if (sourceLastOffTime != null && System.currentTimeMillis() - THREE_DAYS_MILLIS > sourceLastOffTime ){
            lastOffTime = System.currentTimeMillis() - THREE_DAYS_MILLIS;
        }
        //查询所有的会话信息保证换了设备，可以拿到所有的会话消息
        List<ChatSessionUser> chatSessionUserList = chatSessionUserMapper.selectChatSessionContactList(userId);
        WsInitData wsInitData = new WsInitData();
        wsInitData.setChatSessionList(chatSessionUserList);
        //获取离线消息,只拿3天前的消息
        List<String> groupList = contactList.stream().filter(item ->
                item.startsWith(UserContactEnum.GROUP.getPrefix())).collect(Collectors.toList());
        //添加上自己，是为了拿到别人给自己发送的消息
        groupList.add(userId);
        QueryWrapper<ChatMessage> chatMessageQueryWrapper = new QueryWrapper<>();
        chatMessageQueryWrapper.in("contactId", groupList);
        chatMessageQueryWrapper.ge("sendTime", lastOffTime);
        List<ChatMessage> chatMessageList = chatMessageService.list(chatMessageQueryWrapper);
        wsInitData.setChatMessageList(chatMessageList);
        //获取申请消息
        QueryWrapper<UserContactApply> userContactApplyQueryWrapper = new QueryWrapper<>();
        userContactApplyQueryWrapper.eq("receiveUserId", userId);
        //只提示3天前的申请
        userContactApplyQueryWrapper.ge("lastApplyTime",lastOffTime);
        long count = userContactApplyService.count(userContactApplyQueryWrapper);
        wsInitData.setApplyCount(count);
        //发送消息
        MessageSendDTO messageSendDTO = new MessageSendDTO();
        messageSendDTO.setMessageType(MessageTypeEnum.INIT.getType());
        messageSendDTO.setContactId(userId);
        //发送会话记录
        messageSendDTO.setExtendData(wsInitData);
        sendMessage(messageSendDTO, userId);
    }



    public void sendMes(MessageSendDTO messageSendDTO){
        String contactId = messageSendDTO.getContactId();
        UserContactEnum userContactEnum = UserContactEnum.getEnumByPrefix(contactId);
        if (userContactEnum == null){
            return;
        }
        switch (userContactEnum){
            case USER:
                sendToUser(messageSendDTO);
                break;
            case GROUP:
                sendToGroup(messageSendDTO);
                break;
        }
    }

    /**
     * 发送给用户
     * @param messageSendDTO
     */
    public void sendToUser(MessageSendDTO messageSendDTO){
        String contactId = messageSendDTO.getContactId();
        if (StringUtils.isEmpty(contactId)){
            return;
        }
        //发送消息
        sendMessage(messageSendDTO,contactId);
        //强制下线
        if (MessageTypeEnum.FORCE_OFF_LINE.getType().equals(messageSendDTO.getMessageType())){
           closeContext(contactId);
        }
    }


    /**
     * !
     *
     * @param messageSendDTO
     * @param receiveUserId  接收人
     */
    public void sendMessage(MessageSendDTO messageSendDTO, String receiveUserId) {
        if (receiveUserId == null) {
            log.error("receiveUserId is null");
            return;
        }
        Channel userChanel = USER_CHANNEL_CACHE.getIfPresent(receiveUserId);
        if (userChanel == null) {
            log.error("channel is null");
            return;
        }
        //相对于客户端而已，自己既是发送人也是接收人,可以理解为是系统给他发的消息
        //b->a,相对于a来说，b是联系人所以消息的ContactId就是发送人，所以ContactName就是发送人，两者是相对的
        if (MessageTypeEnum.ADD_FRIEND_SELF.getType().equals(messageSendDTO.getMessageType())){
            Object extendData = messageSendDTO.getExtendData();
            User userInfo= BeanUtil.copyProperties(extendData, User.class);
            messageSendDTO.setContactId(userInfo.getId());
            messageSendDTO.setContactName(userInfo.getUserName());
            messageSendDTO.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
            messageSendDTO.setExtendData(null);
        }else {
            messageSendDTO.setContactId(messageSendDTO.getSenderUserId());
            messageSendDTO.setContactName(messageSendDTO.getSendUserName());
        }
        //发送消息
        userChanel.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(messageSendDTO)));
    }

    /**
     * 被踢下线后断开连接
     * @param userId
     */
    public void closeContext(String userId) {
        if (StringUtils.isEmpty(userId)){
            return;
        }
        Channel channel = USER_CHANNEL_CACHE.getIfPresent(userId);
        if (channel == null){
            return;
        }
        channel.close();
    }

    /**
     * 发送群组
     * @param messageSendDTO
     */
    public void sendToGroup(MessageSendDTO messageSendDTO) {
        String groupId = messageSendDTO.getContactId();
        if (StringUtils.isEmpty(groupId)){
            return;
        }
        ChannelGroup groupChannel = GROUP_CHANNEL_CACHE.getIfPresent(groupId);
        if (groupChannel == null){
            return;
        }
        groupChannel.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(messageSendDTO)));
    }



    /**
     * 添加群聊组
     * @param groupId
     * @param userId
     */
    public void addGroupContext(String groupId, String userId) {
        ChannelGroup groupChannel = GROUP_CHANNEL_CACHE.getIfPresent(groupId);
        Channel channel = USER_CHANNEL_CACHE.getIfPresent(userId);
        if (groupChannel == null) {
            //使用GlobalEventExecutor.INSTANCE意味着这个ChannelGroup的操作将在全局的事件执行器上执行
            groupChannel = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CHANNEL_CACHE.put(groupId, groupChannel);
        }
        if (channel == null) {
            log.error("channel is null");
            return;
        }
        groupChannel.add(channel);
    }

    /**
     * 移除channel，并且更新用户最后在线时间
     * @param channel
     */
    public void removeContext(Channel channel) {
        Attribute<String> attr = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attr.get();
        if (userId == null) {
            return;
        }
        //将链接清除
        USER_CHANNEL_CACHE.invalidate(userId);
        //移除心跳
        redisUtils.removeHeartBeatTime(userId);
        //更新用户最后离线时间
        User updataUser = new User();
        updataUser.setId(userId);
        updataUser.setLastOffTime(System.currentTimeMillis());
        userService.updateById(updataUser);
    }
}

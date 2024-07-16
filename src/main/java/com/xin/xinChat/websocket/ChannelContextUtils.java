package com.xin.xinChat.websocket;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xin.xinChat.model.entity.ChatSessionUser;
import com.xin.xinChat.model.entity.User;
import com.xin.xinChat.model.enums.UserContactEnum;
import com.xin.xinChat.service.ChatSessionService;
import com.xin.xinChat.service.ChatSessionUserService;
import com.xin.xinChat.service.UserService;
import com.xin.xinChat.utils.RedisUtils;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Date;
import java.util.List;

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
    private ChatSessionService chatSessionService;

    @Resource
    private ChatSessionUserService chatSessionUserService;

    private static final Cache<String,Channel> USER_CHANNEL_CACHE = Caffeine.newBuilder().
            maximumSize(10000).
            expireAfterWrite(Duration.ofSeconds(REDIS_HEART_BEAT_TIME)).build();
    private static final Cache<String, ChannelGroup> GROUP_CHANNEL_CACHE = Caffeine.newBuilder().
            maximumSize(100).
            expireAfterWrite(Duration.ofSeconds(REDIS_HEART_BEAT_TIME)).build();
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
                addGroupContext(contactId, channel);
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
            lastOffTime = THREE_DAYS_MILLIS;
        }
        //查询所有的会话信息保证换了设备，可以拿到所有的会话消息
        QueryWrapper<ChatSessionUser> chatSessionUserQueryWrapper = new QueryWrapper<>();
        chatSessionUserQueryWrapper.eq("userId",userId);
        chatSessionUserQueryWrapper.orderByDesc("lastReceiveTime");
        List<ChatSessionUser> chatSessionUserList = chatSessionUserService.list(chatSessionUserQueryWrapper);
        WsInitData wsInitData = new WsInitData();
        wsInitData.setChatSessionUserList(chatSessionUserList);

        //todo 获取离线消息

        //todo 获取申请消息


    }

    //发送消息
    public static void sendMessage(){

    }

    public void addGroupContext(String groupId, Channel channel) {
        ChannelGroup groupChannel = GROUP_CHANNEL_CACHE.getIfPresent(groupId);
        if (groupChannel == null){
            //使用GlobalEventExecutor.INSTANCE意味着这个ChannelGroup的操作将在全局的事件执行器上执行
            groupChannel = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CHANNEL_CACHE.put(groupId,groupChannel);
        }
        if (channel == null ){
            log.error("channel is null");
            return;
        }
        groupChannel.add(channel);
    }

    public void removeContext(Channel channel) {
        Attribute<String> attr = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attr.get();
        if (userId != null){
            //将链接清除
            USER_CHANNEL_CACHE.invalidate(userId);
        }
        //移除心跳
        redisUtils.removeHeartBeatTime(userId);
        //更新用户最后离线时间
        User updataUser = new User();
        updataUser.setLastOffTime(System.currentTimeMillis());
        userService.updateById(updataUser);
    }
}

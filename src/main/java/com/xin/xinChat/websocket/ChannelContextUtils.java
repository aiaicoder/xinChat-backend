package com.xin.xinChat.websocket;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xin.xinChat.utils.RedisUtils;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.time.Duration;

import static com.xin.xinChat.constant.RedisKeyConstant.REDIS_HEART_BEAT_TIME;

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
        //将chanel放到缓存中管理起来
        USER_CHANNEL_CACHE.put(userId,channel);
        //发送心跳
        redisUtils.setHeartBeatTime(userId);
        String groupId = "G10000";
        addGroupContext(groupId,channel);
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

    public void sentToGroup(String groupId, String msg) {
        ChannelGroup groupChannel = GROUP_CHANNEL_CACHE.getIfPresent(groupId);
        groupChannel.writeAndFlush(new TextWebSocketFrame(msg));
    }
}

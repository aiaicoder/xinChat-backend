package com.xin.xinChat.websocket.netty;

import cn.dev33.satoken.stp.StpUtil;
import com.xin.xinChat.utils.RedisUtils;
import com.xin.xinChat.websocket.ChannelContextUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/13 20:06
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class HandlerWebSocket extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    @Resource
    private ChannelContextUtils channelContextUtils;

    @Resource
    RedisUtils redisUtils;


    /**
     * 通道就绪后调用，一般用户用来做初始化
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有新的链接加入");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("链接断开");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {
        Channel channel = ctx.channel();
        Attribute<String> attr = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attr.get();
        //收到消息保存心跳
        redisUtils.setHeartBeatTime(userId);
        channelContextUtils.sentToGroup("G10000", textWebSocketFrame.text());
        log.info("接收到消息userId：{}的消息：{}", userId, textWebSocketFrame.text());
    }

    public Object getUser(String token){
        return StpUtil.getLoginIdByToken(token);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //因为websocket是基于tcp的长连接，所以需要判断是否是握手完成事件
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            log.info("握手成功");
            String url = complete.requestUri();
            String token = getToken(url);
            if (token == null) {
                log.info("token为空");
                ctx.close();
                return;
            }
            String userId = (String) getUser(token);
            if (userId == null) {
                log.info("token非法");
                ctx.close();
                return;
            }
            channelContextUtils.addContext(userId, ctx.channel());
            log.info("url:{}", url);
        }
    }

    public String getToken(String url){
        if (StringUtils.isEmpty(url) || !url.contains("?")){
            return null;
        }
        String[] queryParams = url.split("\\?");
        if (queryParams.length != 2){
            return null;
        }
        String queryParam = queryParams[1];
        if (StringUtils.isEmpty(queryParam)){
            return null;
        }
        String[] params = queryParam.split("=");
        if (params.length != 2){
            return null;
        }
        return params[1];
    }
}

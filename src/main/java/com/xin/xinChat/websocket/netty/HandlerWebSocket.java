package com.xin.xinChat.websocket.netty;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.xin.xinChat.model.vo.UserVO;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import static com.xin.xinChat.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/13 20:06
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class HandlerWebSocket extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    /**
     * 通道就绪后调用，一般用户用来做初始化
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
        log.info("接收到消息：{}",textWebSocketFrame.text());
    }

    public Object getUser(String token){
        return StpUtil.getLoginIdByToken(token);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //因为websocket是基于tcp的长连接，所以需要判断是否是握手完成事件
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete){
            WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            log.info("握手成功");
            String url = complete.requestUri();
            String token = getToken(url);
            Object user = getUser(token);
            if (user == null){
                log.info("非法请求");
                ctx.close();
                return;
            }
            if (token == null){
                log.info("token为空");
                ctx.close();
                return;
            }
            log.info("url:{}",url);
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

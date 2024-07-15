package com.xin.xinChat.websocket.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/13 19:56
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class HandlerHeartBeat extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE){
                Channel channel = ctx.channel();
                Attribute<String> attr = channel.attr(AttributeKey.valueOf(channel.id().toString()));
                String userId = attr.get();
                ctx.close();
                log.info("用户：{}心跳超时",userId);

            }

        }
    }
}

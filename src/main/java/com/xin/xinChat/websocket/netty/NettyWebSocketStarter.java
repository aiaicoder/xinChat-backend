package com.xin.xinChat.websocket.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/13 19:45
 */
@Slf4j
public class NettyWebSocketStarter {
    //创建两个线程池
    //一个是接受消息的一个处理消息的
    private static EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private static EventLoopGroup workerGroup = new NioEventLoopGroup();

    public static void main(String[] args) {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class).
                    handler(new LoggingHandler(LogLevel.DEBUG)).
                    childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            //设置几个重要的处理器
                            //对http协议的支持，使用http的编码器，解码器
                            pipeline.addLast(new HttpServerCodec());
                            //聚合解码器 可以处理httpRequest/httpContent/lastHttpContent到fullHttpRequest
                            //保证接受的http请求的完整性
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            //心跳 long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit
                            // readerIdleTime:读超时时间，writerIdleTime:写超时时间，allIdleTime:读写超时时间
                            pipeline.addLast(new IdleStateHandler(6,0,0, TimeUnit.SECONDS));
                            //根据心跳的状态进行处理
                            pipeline.addLast(new HandlerHeartBeat());
                            //将http协议升级为ws协议
                            pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, true, 65536,true, true,10000L));
                            pipeline.addLast(new HandlerWebSocket());
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(5051).sync();
            log.info("启动成功");
            channelFuture.channel().closeFuture().sync();

        }catch (InterruptedException e) {
            log.error("启动失败");
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }
}

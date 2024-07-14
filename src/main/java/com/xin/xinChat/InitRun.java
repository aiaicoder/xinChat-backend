package com.xin.xinChat;

import com.xin.xinChat.websocket.netty.NettyWebSocketStarter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/14 19:56
 */
@Slf4j
@Component
public class InitRun implements ApplicationRunner {

    @Resource
    private NettyWebSocketStarter nettyWebSocketStarter;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            nettyWebSocketStarter.start();
        } catch (Exception e) {
            log.error("netty启动失败", e);
        }
    }
}

package com.xin.xinChat.config;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/6/12 18:51
 */

@Component("appConfig")
@Data
public class AppConfig {

    /**
     * websocket 连接地址
     */
    @Value("${ws.port:8104}")
    public Integer wsPort;

    /**
     * 管理员邮箱
     */
    @Value("${admin.email}")
    public String adminEmail;


}

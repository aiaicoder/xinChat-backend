package com.xin.xinChat.config.MQ;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.xin.xinChat.constant.MqConstant.MESSAGE_EXCHANGE;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/20 15:12
 */
@Configuration
public class MqConfig {


    @Bean
    public FanoutExchange messageExchange() {
        return new FanoutExchange(MESSAGE_EXCHANGE, true, false);
    }
}

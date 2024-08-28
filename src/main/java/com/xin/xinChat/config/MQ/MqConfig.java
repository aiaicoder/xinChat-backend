package com.xin.xinChat.config.MQ;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.xin.xinChat.constant.MqConstant.MESSAGE_EXCHANGE;
import static com.xin.xinChat.constant.MqConstant.MESSAGE_QUEUE;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/20 15:12
 */
@Configuration
public class MqConfig {


    @Bean
    Queue queue() {
        // 定义持久化队列
        return QueueBuilder.durable(MESSAGE_QUEUE).build();
    }

    @Bean
    FanoutExchange fanoutExchange() {
        // 定义 fanout 交换机
        return new FanoutExchange(MESSAGE_EXCHANGE);
    }

    @Bean
    Binding binding(Queue queue, FanoutExchange fanoutExchange) {
        // 绑定队列到交换机
        return BindingBuilder.bind(queue).to(fanoutExchange);
    }
}

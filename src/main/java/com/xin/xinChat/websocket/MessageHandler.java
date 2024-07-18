package com.xin.xinChat.websocket;

import cn.hutool.json.JSONUtil;
import com.rabbitmq.client.Channel;
import com.xin.xinChat.model.dto.Message.MessageSendDTO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.xin.xinChat.constant.MqConstant.MESSAGE_EXCHANGE;

/**
 * @author <a href="https://github.com/aiaicoder">  小新
 * @version 1.0
 * @date 2024/7/20 15:11
 */
@Component
@Slf4j
public class MessageHandler {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    @Lazy
    private ChannelContextUtils channelContextUtils;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(), //切记： 此处无需设置队列名称，否在得话，多个消费者只有一个消费者能消费数据。其它消费者无法消费数据。
            exchange = @Exchange(value = MESSAGE_EXCHANGE,type = ExchangeTypes.FANOUT)))
    @SneakyThrows
    private void receiveMessage(MessageSendDTO  messageSendDTO, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        if (messageSendDTO == null){
            channel.basicNack(deliveryTag, false, false);
        }
        String jsonStr = JSONUtil.toJsonStr(messageSendDTO);
        log.info("接收到消息：{}",jsonStr);
        //发送消息
        channelContextUtils.sendMes(messageSendDTO);
        channel.basicAck(deliveryTag, false);
    }




    public void sendMessage(MessageSendDTO messageSendDTO) {
        rabbitTemplate.convertAndSend(MESSAGE_EXCHANGE,"",messageSendDTO);
    }
}

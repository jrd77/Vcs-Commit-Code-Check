package com.atzuche.order.coreapi.listener.sms;

import com.alibaba.fastjson.JSONObject;
import com.atzuche.order.coreapi.listener.push.OrderSendMessageFactory;
import com.atzuche.order.coreapi.listener.push.OrderSendMessageManager;
import com.atzuche.order.mq.common.base.OrderMessage;
import com.dianping.cat.Cat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * @author 胡春林
 * 订单的总action事件处理
 */
@Component
@Slf4j
public class OrderActionEventListener extends OrderSendMessageManager {


    @RabbitListener(bindings = {@QueueBinding(value = @Queue(value = "order_action_08", durable = "true"),
            exchange = @Exchange(value = "auto-order-action", durable = "true", type = "topic"), key = "action.#")
    }, containerFactory = "orderRabbitListenerContainerFactory")
    public void process(Message message) {
        log.info("receive order action message: " + new String(message.getBody()));
        OrderMessage orderMessage = JSONObject.parseObject(message.getBody(), OrderMessage.class);
        log.info("新订单动作总事件监听,入参orderMessage:[{}]", orderMessage.toString());
        try {
            sendSMSMessageData(orderMessage);
            sendPushMessageData(orderMessage);
        } catch (Exception e) {
            log.info("新订单动作总事件监听发生异常,msg：[{}]", e);
            Cat.logError("新订单动作总事件监听发生异常", e);
        }
    }

}
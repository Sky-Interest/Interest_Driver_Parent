package com.gec.interest.payment.receiver;

import com.gec.interest.common.constant.MqConst;
import com.gec.interest.payment.service.WxPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import com.rabbitmq.client.Channel;

@Slf4j
@Component
public class PaymentReceiver {

    @Autowired
    private WxPayService wxPayService;

    /**
     * 订单支付成功，处理支付回调
     *
     * @param orderNo
     * @throws IOException
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_PAY_SUCCESS, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_ORDER),
            key = {MqConst.ROUTING_PAY_SUCCESS}
    ))
    public void paySuccess(String orderNo, Message message, Channel channel) throws IOException {
        wxPayService.handleOrder(orderNo);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}

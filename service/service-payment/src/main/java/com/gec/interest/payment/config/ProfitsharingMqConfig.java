package com.gec.interest.payment.config;

import com.gec.interest.common.constant.MqConst;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ProfitsharingMqConfig {

    //声明了一个队列
    @Bean
    public Queue profitsharingQueue() {
        // 第一个参数是创建的queue的名字，第二个参数是是否支持持久化
        return new Queue(MqConst.QUEUE_PROFITSHARING, true);
    }

    //声明了一个交换机Exchange
    @Bean
    public CustomExchange profitsharingExchange() {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(MqConst.EXCHANGE_PROFITSHARING, "x-delayed-message", true, false, args);
    }

    //指定会列和交换机的绑定关系
    @Bean
    public Binding bindingCancel() {
        return BindingBuilder.bind(profitsharingQueue()).to(profitsharingExchange()).with(MqConst.ROUTING_PROFITSHARING).noargs();
    }

}

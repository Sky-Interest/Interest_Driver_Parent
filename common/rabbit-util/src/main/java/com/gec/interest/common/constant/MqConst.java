package com.gec.interest.common.constant;

public class MqConst {


    public static final String EXCHANGE_ORDER = "interest.order";
    public static final String ROUTING_PAY_SUCCESS = "interest.pay.success";
    public static final String ROUTING_PROFITSHARING_SUCCESS = "interest.profitsharing.success";
    public static final String QUEUE_PAY_SUCCESS = "interest.pay.success";
    public static final String QUEUE_PROFITSHARING_SUCCESS = "interest.profitsharing.success";


    //取消订单延迟消息
    public static final String EXCHANGE_CANCEL_ORDER = "interest.cancel.order";
    public static final String ROUTING_CANCEL_ORDER = "interest.cancel.order";
    public static final String QUEUE_CANCEL_ORDER = "interest.cancel.order";

    //分账延迟消息
    public static final String EXCHANGE_PROFITSHARING = "interest.profitsharing";
    public static final String ROUTING_PROFITSHARING = "interest.profitsharing";
    public static final String QUEUE_PROFITSHARING  = "interest.profitsharing";

}

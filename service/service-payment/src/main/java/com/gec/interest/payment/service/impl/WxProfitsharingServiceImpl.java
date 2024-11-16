package com.gec.interest.payment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gec.interest.common.constant.MqConst;
import com.gec.interest.common.constant.SystemConstant;
import com.gec.interest.common.execption.interestException;
import com.gec.interest.common.result.ResultCodeEnum;
import com.gec.interest.common.service.RabbitService;
import com.gec.interest.model.entity.payment.PaymentInfo;
import com.gec.interest.model.entity.payment.ProfitsharingInfo;
import com.gec.interest.model.form.payment.ProfitsharingForm;
import com.gec.interest.payment.config.WxPayV3Properties;
import com.gec.interest.payment.mapper.PaymentInfoMapper;
import com.gec.interest.payment.mapper.ProfitsharingInfoMapper;
import com.gec.interest.payment.service.WxProfitsharingService;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.profitsharing.ProfitsharingService;
import com.wechat.pay.java.service.profitsharing.model.*;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Random;
import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class WxProfitsharingServiceImpl implements WxProfitsharingService {
    @Autowired
    private PaymentInfoMapper paymentInfoMapper;

    @Autowired
    private ProfitsharingInfoMapper profitsharingInfoMapper;

    @Autowired
    private WxPayV3Properties wxPayV3Properties;

    @Autowired
    private RSAAutoCertificateConfig rsaAutoCertificateConfig;

    @Autowired
    private RabbitService rabbitService;

    //分账的业务方法处理
    @Override
    public void profitsharing(ProfitsharingForm profitsharingForm) {
        //1、判断分账是否成功、如果已经分账了就不再进行处理
        Long count = profitsharingInfoMapper.selectCount(
                new LambdaQueryWrapper<ProfitsharingInfo>()
                        .eq(ProfitsharingInfo::getOrderNo, profitsharingForm.getOrderNo())
        );
        if (count > 0) {//已经分账，则不再处理
            return;
        }
        //2、获取payment信息、根据订单编号获取
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(
                new LambdaQueryWrapper<PaymentInfo>()
                        .eq(PaymentInfo::getOrderNo, profitsharingForm.getOrderNo())
        );
        //3、构建分账业务对象
        ProfitsharingService service = new ProfitsharingService.Builder().config(rsaAutoCertificateConfig).build();
        //4、构建分账的请求参数  createOrder中提供 createOrderRequest \ createOrderRequest中有提供接受者列表
        //4-1:构建分账接收方参数 接收方的信息【日志记录】
        AddReceiverRequest addReceiverRequest = new AddReceiverRequest();
        addReceiverRequest.setAppid(wxPayV3Properties.getAppid());
        addReceiverRequest.setType(ReceiverType.PERSONAL_OPENID);
        addReceiverRequest.setAccount(paymentInfo.getDriverOpenId());
        addReceiverRequest.setRelationType(ReceiverRelationType.PARTNER);
        AddReceiverResponse addReceiverResponse = service.addReceiver(addReceiverRequest);
        log.info("添加分账接收方：{}", JSONObject.toJSONString(addReceiverResponse));
        //4-2：基础的请求信息 \分账订单编号
        CreateOrderRequest createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setAppid(wxPayV3Properties.getAppid());
        createOrderRequest.setTransactionId(paymentInfo.getTransactionId());
        //算法：OrderNo+随机数 ==outOrderNo
        String outOrderNo = profitsharingForm.getOrderNo() + "_" + new Random().nextInt(10);
        createOrderRequest.setOutOrderNo(outOrderNo);
        //分账接收方列表
        List<CreateOrderReceiver> receivers = new ArrayList<>();
        CreateOrderReceiver orderReceiver = new CreateOrderReceiver();
        orderReceiver.setType("PERSONAL_OPENID");
        orderReceiver.setAccount(paymentInfo.getDriverOpenId());
        //4-3：分账金额-分账信息中的金额单位制元-[但是在微信支付中Amount单位是分]
        long amout = profitsharingForm.getAmount().multiply(new BigDecimal("100")).longValue();
        orderReceiver.setAmount(amout);
        orderReceiver.setDescription("司机代驾的分账");
        receivers.add(orderReceiver);
        //createOrderRequest设置接收方列表
        createOrderRequest.setReceivers(receivers);
        //是否解冻剩余未分资金,如果为true,该笔订单剩余未分账的金额会解冻回分账方商户
        createOrderRequest.setUnfreezeUnsplit(true);
        //5、执行分账[创建分账的账单]  createOrderRequest进行入参
        OrdersEntity ordersEntity = service.createOrder(createOrderRequest);
        //6、分账结果的逻辑判断处理
        if (ordersEntity.getState().name().equals("FINISHED")) {//完成的状态
            //构建封装返回分账信息
            ProfitsharingInfo profitsharingInfo = new ProfitsharingInfo();
            profitsharingInfo.setOrderNo(paymentInfo.getOrderNo());
            profitsharingInfo.setTransactionId(paymentInfo.getTransactionId());
            profitsharingInfo.setOutTradeNo(outOrderNo);
            profitsharingInfo.setAmount(profitsharingInfo.getAmount());
            profitsharingInfo.setState(ordersEntity.getState().name());
            profitsharingInfo.setResponeContent(JSONObject.toJSONString(ordersEntity));
            //将记录添加到业务表中order_profitsharing
            profitsharingInfoMapper.insert(profitsharingInfo);
            //通过mq发送信息[告知分账处理成功、继续下一步操作（更新分账信息）]
            rabbitService.sendMessage(MqConst.EXCHANGE_ORDER, MqConst.ROUTING_PROFITSHARING_SUCCESS, paymentInfo.getOrderNo());
        } else if (ordersEntity.getState().name().equals("PROCESSING")) {//PROCESSING：处理中
            //通过延迟队列发送一个延迟消息、然后监听者在一段时间之后再消费信息去进行分账
            rabbitService.sendDealyMessage(
                    MqConst.EXCHANGE_PROFITSHARING,
                    MqConst.ROUTING_PROFITSHARING,
                    JSONObject.toJSONString(profitsharingForm),
                    SystemConstant.PROFITSHARING_DELAY_TIME
            );
        } else {
            log.error("执行分账失败");
            throw new interestException(ResultCodeEnum.PROFITSHARING_FAIL);
        }
    }

}

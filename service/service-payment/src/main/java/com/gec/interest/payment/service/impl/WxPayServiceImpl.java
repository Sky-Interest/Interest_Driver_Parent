package com.gec.interest.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gec.interest.common.constant.MqConst;
import com.gec.interest.common.execption.interestException;
import com.gec.interest.common.result.ResultCodeEnum;
import com.gec.interest.common.service.RabbitService;
import com.gec.interest.common.util.RequestUtils;
import com.gec.interest.driver.client.DriverAccountFeignClient;
import com.gec.interest.model.entity.payment.PaymentInfo;
import com.gec.interest.model.enums.TradeType;
import com.gec.interest.model.form.driver.TransferForm;
import com.gec.interest.model.form.payment.PaymentInfoForm;
import com.gec.interest.model.vo.order.OrderRewardVo;
import com.gec.interest.model.vo.payment.WxPrepayVo;
import com.gec.interest.order.client.OrderInfoFeignClient;
import com.gec.interest.payment.config.WxPayV3Properties;
import com.gec.interest.payment.mapper.PaymentInfoMapper;
import com.gec.interest.payment.service.WxPayService;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import com.wechat.pay.java.service.payments.model.Transaction;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class WxPayServiceImpl implements WxPayService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    RSAAutoCertificateConfig rsaAutoCertificateConfig;
    @Autowired
    WxPayV3Properties wxPayV3Properties;
    @Autowired
    private RabbitService rabbitService;
    @Autowired
    private OrderInfoFeignClient orderInfoFeignClient;
    @Autowired
    private DriverAccountFeignClient driverAccountFeignClient;

    @Override
    public WxPrepayVo createWxPayment(PaymentInfoForm paymentInfoForm) {
        try {
            //1、把订单支付的数据信获取、判断记录是否存在，不在则插入数据
            PaymentInfo paymentInfo = paymentInfoMapper.selectOne(
                    new LambdaQueryWrapper<PaymentInfo>()
                            .eq(PaymentInfo::getOrderNo, paymentInfoForm.getOrderNo())
            );
            if (paymentInfo == null) {
                PaymentInfo info = new PaymentInfo();
                BeanUtils.copyProperties(paymentInfoForm, info);
                info.setPaymentStatus(0);//默认为未支付状态
            }
            //2、JSapi-Service接口构建、注入支付密钥的config配置对象
            JsapiServiceExtension service = new JsapiServiceExtension.Builder()
                    .config(rsaAutoCertificateConfig).build();
            //2-1提供request、参数
            PrepayRequest request = new PrepayRequest();
            Amount amount = new Amount();
            //金额的设置
            amount.setTotal(paymentInfoForm.getAmount().multiply(new BigDecimal(100)).intValue());
            request.setAmount(amount);
            request.setAppid(wxPayV3Properties.getAppid());
            request.setMchid(wxPayV3Properties.getMerchantId());
            //支付的描述
            String description = paymentInfo.getContent();
            if (description.length() > 127) {//字符串截取【限制长度不能超过127个字符】
                description = description.substring(0, 127);
            }
            request.setDescription(description);
            //回调的请求url
            request.setNotifyUrl(wxPayV3Properties.getNotifyUrl());
            request.setOutTradeNo(paymentInfo.getOrderNo());
            //用户信息【openid】
            Payer payer = new Payer();
            payer.setOpenid(paymentInfoForm.getCustomerOpenId());
            request.setPayer(payer);
            //分账配置[商家收款、分账给司机]
            SettleInfo settleInfo = new SettleInfo();
            settleInfo.setProfitSharing(true);
            request.setSettleInfo(settleInfo);
            //发起请求
            PrepayWithRequestPaymentResponse response = service.prepayWithRequestPayment(request);
            log.info("微信支付下单返回参数：{}", JSONObject.toJSONString(response));
            //封装数据并且返回
            WxPrepayVo wxPrepayVo = new WxPrepayVo();
            BeanUtils.copyProperties(response, wxPrepayVo);
            wxPrepayVo.setTimeStamp(response.getTimeStamp());
            return wxPrepayVo;
        } catch (Exception e) {
            throw new interestException(ResultCodeEnum.WX_CREATE_ERROR);
        }
    }
    @Transactional

//微信支付响应后的异步处理的业务方法
    @Override
    public Map<String, Object> wxnotify(HttpServletRequest request) {
        //1、获取回调通知的请求参数【校验签名、解密】
        //1.回调通知的验签与解密\从request头信息获取参数
        //HTTP 头 Wechatpay-Signature\ Wechatpay-Nonce\Wechatpay-Timestamp\Wechatpay-Serial\ Wechatpay-Signature-Type
        //HTTP 请求体 body。切记使用原始报文，不要用 JSON 对象序列化后的字符串，避免验签的 body 和原文不一致。
        String wechatPaySerial = request.getHeader("Wechatpay-Serial");
        String nonce = request.getHeader("Wechatpay-Nonce");
        String timestamp = request.getHeader("Wechatpay-Timestamp");
        String signature = request.getHeader("Wechatpay-Signature");
        String requestBody = RequestUtils.readData(request);
        log.info("wechatPaySerial：{}", wechatPaySerial);
        log.info("nonce：{}", nonce);
        log.info("timestamp：{}", timestamp);
        log.info("signature：{}", signature);
        log.info("requestBody：{}", requestBody);
        //2、构建请求参数
        RequestParam requestParam = new RequestParam.Builder()
                .serialNumber(wechatPaySerial)
                .nonce(nonce)
                .signature(signature)
                .timestamp(timestamp)
                .body(requestBody)
                .build();
        //提供一个消息的解析器
        NotificationParser parser = new NotificationParser(rsaAutoCertificateConfig);
        //3、调用支付处理的业务逻辑代码【更新支付数据、更新订单状态....】
        Transaction transaction = parser.parse(requestParam, Transaction.class);
        log.info("成功解析：{}", JSONObject.toJSONString(transaction));
        //判断交易是否成功
        if (null != transaction && transaction.getTradeState() == Transaction.TradeStateEnum.SUCCESS) {
            //5.处理支付成功后的业务实现
            this.handlePayment(transaction);
        }
        //保留数据返回
        return null;
    }

    public void handlePayment(Transaction transaction) {
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(new LambdaQueryWrapper<PaymentInfo>()
                .eq(PaymentInfo::getOrderNo, transaction.getOutTradeNo()));
        if (paymentInfo.getPaymentStatus() == 1) {
            return;
        }

        //更新支付信息
        paymentInfo.setPaymentStatus(1);
        paymentInfo.setOrderNo(transaction.getOutTradeNo());
        paymentInfo.setTransactionId(transaction.getTransactionId());
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(JSON.toJSONString(transaction));
        paymentInfoMapper.updateById(paymentInfo);
        // 表示交易成功！

        // 后续更新订单状态！ 使用消息队列！
        rabbitService.sendMessage(MqConst.EXCHANGE_ORDER, MqConst.ROUTING_PAY_SUCCESS, paymentInfo.getOrderNo());
    }
    @Override
    public Boolean queryPayStatus(String orderNo) {
        // 构建service
        JsapiServiceExtension service = new JsapiServiceExtension.Builder().config(rsaAutoCertificateConfig).build();

        QueryOrderByOutTradeNoRequest queryRequest = new QueryOrderByOutTradeNoRequest();
        queryRequest.setMchid(wxPayV3Properties.getMerchantId());
        queryRequest.setOutTradeNo(orderNo);

        try {
            Transaction transaction = service.queryOrderByOutTradeNo(queryRequest);
            log.info(JSON.toJSONString(transaction));
            if(null != transaction && transaction.getTradeState() == Transaction.TradeStateEnum.SUCCESS) {
                //更改订单状态
                this.handlePayment(transaction);
                return true;
            }
        } catch (ServiceException e) {
            // API返回失败, 例如ORDER_NOT_EXISTS
            System.out.printf("code=[%s], message=[%s]\n", e.getErrorCode(), e.getErrorMessage());
        }
        return false;
    }
    //@GlobalTransactional//分布式事务注解
    @Override
    public void handleOrder(String orderNo) {
        //1.更改订单支付状态
        orderInfoFeignClient.updateOrderPayStatus(orderNo);

        //2.处理系统奖励，打入司机账户
        OrderRewardVo orderRewardVo = orderInfoFeignClient.getOrderRewardFee(orderNo).getData();
        if(null != orderRewardVo.getRewardFee() && orderRewardVo.getRewardFee().doubleValue() > 0) {
            TransferForm transferForm = new TransferForm();
            transferForm.setTradeNo(orderNo);
            transferForm.setTradeType(TradeType.REWARD.getType());
            transferForm.setContent(TradeType.REWARD.getContent());
            transferForm.setAmount(orderRewardVo.getRewardFee());
            transferForm.setDriverId(orderRewardVo.getDriverId());
            driverAccountFeignClient.transfer(transferForm);
        }

        //3.TODO分账
    }


}

package com.gec.interest.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gec.interest.common.execption.interestException;
import com.gec.interest.common.result.ResultCodeEnum;
import com.gec.interest.model.entity.payment.PaymentInfo;
import com.gec.interest.model.form.payment.PaymentInfoForm;
import com.gec.interest.model.vo.payment.WxPrepayVo;
import com.gec.interest.payment.config.WxPayV3Properties;
import com.gec.interest.payment.mapper.PaymentInfoMapper;
import com.gec.interest.payment.service.WxPayService;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class WxPayServiceImpl implements WxPayService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    RSAAutoCertificateConfig rsaAutoCertificateConfig;
    @Autowired
    WxPayV3Properties wxPayV3Properties;

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
}

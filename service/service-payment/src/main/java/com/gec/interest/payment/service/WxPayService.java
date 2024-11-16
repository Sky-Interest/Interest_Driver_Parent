package com.gec.interest.payment.service;

import com.gec.interest.model.form.payment.PaymentInfoForm;
import com.gec.interest.model.vo.payment.WxPrepayVo;

public interface WxPayService {


    WxPrepayVo createWxPayment(PaymentInfoForm paymentInfoForm);
}

package com.gec.interest.payment.service;

import com.gec.interest.model.form.payment.PaymentInfoForm;
import com.gec.interest.model.vo.payment.WxPrepayVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface WxPayService {


    WxPrepayVo createWxPayment(PaymentInfoForm paymentInfoForm);

    Map wxnotify(HttpServletRequest request);
}

package com.gec.interest.customer.service;

import com.gec.interest.model.vo.base.PageVo;
import com.gec.interest.model.vo.coupon.NoReceiveCouponVo;

public interface CouponService  {


    PageVo<NoReceiveCouponVo> findNoReceivePage(Long customerId, Long page, Long limit);
}

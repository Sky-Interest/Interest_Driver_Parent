package com.gec.interest.customer.service;

import com.gec.interest.model.vo.base.PageVo;
import com.gec.interest.model.vo.coupon.NoReceiveCouponVo;
import com.gec.interest.model.vo.coupon.NoUseCouponVo;
import com.gec.interest.model.vo.coupon.UsedCouponVo;

public interface CouponService  {


    PageVo<NoReceiveCouponVo> findNoReceivePage(Long customerId, Long page, Long limit);

    PageVo<NoUseCouponVo> findNoUsePage(Long customerId, Long page, Long limit);

    PageVo<UsedCouponVo> findUsedPage(Long customerId, Long page, Long limit);

    Boolean receive(Long customerId, Long couponId);
}

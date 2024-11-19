package com.gec.interest.coupon.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.interest.model.entity.coupon.CouponInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.interest.model.vo.base.PageVo;
import com.gec.interest.model.vo.coupon.NoReceiveCouponVo;
import com.gec.interest.model.vo.coupon.NoUseCouponVo;

public interface CouponInfoService extends IService<CouponInfo> {


    PageVo<NoReceiveCouponVo> findNoReceivePage(Page<CouponInfo> pageParam, Long customerId);

    PageVo<NoUseCouponVo> findNoUsePage(Page<CouponInfo> pageParam, Long customerId);
}

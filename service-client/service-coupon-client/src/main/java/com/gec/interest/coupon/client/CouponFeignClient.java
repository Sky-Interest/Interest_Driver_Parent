package com.gec.interest.coupon.client;

import com.gec.interest.common.result.Result;
import com.gec.interest.model.vo.base.PageVo;
import com.gec.interest.model.vo.coupon.NoReceiveCouponVo;
import com.gec.interest.model.vo.coupon.NoUseCouponVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(value = "service-coupon")
public interface CouponFeignClient {
    /**
     * 查询未领取优惠券分页列表
     * @param customerId
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/coupon/info/findNoReceivePage/{customerId}/{page}/{limit}")
    Result<PageVo<NoReceiveCouponVo>> findNoReceivePage(
            @PathVariable("customerId") Long customerId,
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit);
    /**
     * 查询未使用优惠券分页列表
     * @param customerId
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/coupon/info/findNoUsePage/{customerId}/{page}/{limit}")
    Result<PageVo<NoUseCouponVo>> findNoUsePage(
            @PathVariable("customerId") Long customerId,
            @PathVariable("page") Long page,
            @PathVariable("limit") Long limit);


}

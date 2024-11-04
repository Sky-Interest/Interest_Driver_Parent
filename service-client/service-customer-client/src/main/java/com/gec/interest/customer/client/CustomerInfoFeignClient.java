package com.gec.interest.customer.client;

import com.gec.interest.common.result.Result;
import com.gec.interest.model.vo.customer.CustomerLoginVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-customer")
public interface CustomerInfoFeignClient {
    /**
     * 小程序授权登录
     * @param code
     * @return
     */
    @GetMapping("/customer/info/login/{code}")
    Result<Long> login(@PathVariable String code);
    /**
     * 获取客户登录信息
     * @param customerId
     * @return
     */
    @GetMapping("/customer/info/getCustomerLoginInfo/{customerId}")
    Result<CustomerLoginVo> getCustomerLoginInfo(@PathVariable("customerId") Long customerId);
}
package com.gec.interest.customer.service;

import com.gec.interest.model.entity.customer.CustomerInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.interest.model.vo.customer.CustomerLoginVo;

public interface CustomerInfoService extends IService<CustomerInfo> {
    Long login(String code);
    CustomerLoginVo getCustomerLoginInfo(Long customerId);
}

package com.gec.interest.customer.service;


import com.gec.interest.model.vo.customer.CustomerLoginVo;

public interface CustomerService {
    String login(String code);
    CustomerLoginVo getCustomerLoginInfo(Long customerId);
}

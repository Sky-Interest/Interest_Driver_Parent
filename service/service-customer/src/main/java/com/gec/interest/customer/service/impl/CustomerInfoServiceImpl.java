package com.gec.interest.customer.service.impl;

import com.gec.interest.customer.mapper.CustomerInfoMapper;
import com.gec.interest.customer.service.CustomerInfoService;
import com.gec.interest.model.entity.customer.CustomerInfo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CustomerInfoServiceImpl extends ServiceImpl<CustomerInfoMapper, CustomerInfo> implements CustomerInfoService {

}

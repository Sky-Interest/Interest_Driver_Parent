package com.gec.interest.customer.service.impl;

import com.gec.interest.common.constant.RedisConstant;
import com.gec.interest.common.execption.interestException;
import com.gec.interest.common.result.Result;
import com.gec.interest.common.result.ResultCodeEnum;
import com.gec.interest.customer.client.CustomerInfoFeignClient;
import com.gec.interest.customer.service.CustomerService;
import com.gec.interest.model.vo.customer.CustomerLoginVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CustomerServiceImpl implements CustomerService {


    @Autowired
    private CustomerInfoFeignClient customerInfoFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public String login(String code) {
        //获取openId
        //1 拿着code进行远程调用，返回用户id
        Result<Long> result = customerInfoFeignClient.login(code);

        //2 判断如果返回失败了，返回错误提示
        if(result.getCode().intValue() != 200) {
            throw new interestException(result.getCode(), result.getMessage());
        }

        //3 获取远程调用返回用户id
        Long customerId = result.getData();

        //4 判断返回用户id是否为空，如果为空，返回错误提示
        if(null == customerId) {
            throw new interestException(ResultCodeEnum.DATA_ERROR);
        }
        //5 生成token字符串
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        //6 把用户id放到Redis，设置过期时间
        // key:token  value:customerId
        //redisTemplate.opsForValue().set(token,customerId.toString(),30, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX+token, customerId.toString(), RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.SECONDS);

        //7 返回token
        return token;
    }

    @Override
    public CustomerLoginVo getCustomerLoginInfo(Long customerId) {
        Result<CustomerLoginVo> result = customerInfoFeignClient.getCustomerLoginInfo(customerId);
        if (result.getCode().intValue() != 200) {
            throw new interestException(result.getCode(), result.getMessage());
        }
        CustomerLoginVo customerLoginVo = result.getData();
        if (null == customerLoginVo) {
            throw new interestException(ResultCodeEnum.DATA_ERROR);
        }
        return customerLoginVo;
    }

}


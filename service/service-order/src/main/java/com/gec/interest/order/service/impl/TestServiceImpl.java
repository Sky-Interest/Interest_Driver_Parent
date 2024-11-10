package com.gec.interest.order.service.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.gec.interest.order.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public synchronized void testLock() {
        // 查询Redis中的num值
        String value = (String)this.redisTemplate.opsForValue().get("num");
        // 没有该值return
        if (StringUtils.isBlank(value)){
            return ;
        }
        // 有值就转成成int
        int num = Integer.parseInt(value);
        // 把Redis中的num值+1
        this.redisTemplate.opsForValue().set("num", String.valueOf(++num));
    }
}

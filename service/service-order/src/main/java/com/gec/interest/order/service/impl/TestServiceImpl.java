package com.gec.interest.order.service.impl;

import com.alibaba.nacos.common.utils.StringUtils;
import com.gec.interest.order.service.TestService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;


    //使用redisson实现锁的处理
    @Override
    public void testLock() {
        //获取锁对象、尝试获取锁
        RLock lock = redissonClient.getLock("lock");
        lock.lock();//默认超时时间是30s
        //  假设现在业务出现异常  创建【watch dog（60秒）】线程{每10秒检查一下，然后给业务进行延期}
        //业务代码----------------------------------------------------------
        // 查询Redis中的num值、把Redis中的num值+1
        String value = (String) this.redisTemplate.opsForValue().get("num");
        // 没有该值return
        if (StringUtils.isBlank(value)) {
            return;
        }
        // 有值就转成成int
        int num = Integer.parseInt(value);
        // 把Redis中的num值+1
        this.redisTemplate.opsForValue().set("num", String.valueOf(++num));
        //业务代码----------------------------------------------------------
        //删除锁
        lock.unlock();
    }
}

package com.gec.interest.driver.service.impl;

import com.gec.interest.common.constant.RedisConstant;
import com.gec.interest.common.result.Result;
import com.gec.interest.driver.client.DriverInfoFeignClient;
import com.gec.interest.driver.service.DriverService;
import com.gec.interest.model.form.driver.DriverFaceModelForm;
import com.gec.interest.model.form.driver.UpdateDriverAuthInfoForm;
import com.gec.interest.model.vo.driver.DriverAuthInfoVo;
import com.gec.interest.model.vo.driver.DriverLoginVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverServiceImpl implements DriverService {
    @Autowired
    private DriverInfoFeignClient driverInfoFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    //登录
    @Override
    public String login(String code) {
        //远程调用，得到司机id
        Result<Long> longResult = driverInfoFeignClient.login(code);
        //TODO 判断
        Long driverId = longResult.getData();

        //token字符串
        String token = UUID.randomUUID().toString().replaceAll("-","");
        //放到redis，设置过期时间
        redisTemplate.opsForValue().set(RedisConstant.USER_LOGIN_KEY_PREFIX + token,
                driverId.toString(),
                RedisConstant.USER_LOGIN_KEY_TIMEOUT,
                TimeUnit.SECONDS);
        return token;
    }
    @Override
    public DriverLoginVo getDriverLoginInfo(Long driverId) {
        return driverInfoFeignClient.getDriverLoginInfo(driverId).getData();
    }
    @Override
    public DriverAuthInfoVo getDriverAuthInfo(Long driverId) {
        return driverInfoFeignClient.getDriverAuthInfo(driverId).getData();
    }
    @Override
    public Boolean updateDriverAuthInfo(UpdateDriverAuthInfoForm updateDriverAuthInfoForm) {
        return driverInfoFeignClient.UpdateDriverAuthInfo(updateDriverAuthInfoForm).getData();
    }
    @Override
    public Boolean creatDriverFaceModel(DriverFaceModelForm driverFaceModelForm) {
        return driverInfoFeignClient.creatDriverFaceModel(driverFaceModelForm).getData();
    }

}

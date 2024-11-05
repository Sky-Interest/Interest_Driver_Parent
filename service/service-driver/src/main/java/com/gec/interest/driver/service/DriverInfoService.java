package com.gec.interest.driver.service;

import com.gec.interest.model.entity.driver.DriverInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.interest.model.vo.driver.DriverAuthInfoVo;
import com.gec.interest.model.vo.driver.DriverLoginVo;

public interface DriverInfoService extends IService<DriverInfo> {
    Long login(String code);
//    DriverLoginVo getDriverLoginInfo(Long driverId);

    DriverAuthInfoVo getDriverAuthInfo(Long driverId);
}

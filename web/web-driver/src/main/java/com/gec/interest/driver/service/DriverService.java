package com.gec.interest.driver.service;

import com.gec.interest.model.vo.driver.DriverLoginVo;

public interface DriverService {
    String login(String code);
    DriverLoginVo getDriverLoginInfo(Long driverId);

}

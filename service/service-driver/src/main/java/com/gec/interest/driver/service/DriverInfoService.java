package com.gec.interest.driver.service;

import com.gec.interest.model.entity.driver.DriverInfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface DriverInfoService extends IService<DriverInfo> {
    Long login(String code);

}

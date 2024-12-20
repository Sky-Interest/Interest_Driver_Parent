package com.gec.interest.driver.service;

import com.gec.interest.model.entity.driver.DriverInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.interest.model.entity.driver.DriverSet;
import com.gec.interest.model.form.driver.DriverFaceModelForm;
import com.gec.interest.model.form.driver.UpdateDriverAuthInfoForm;
import com.gec.interest.model.vo.driver.DriverAuthInfoVo;
import com.gec.interest.model.vo.driver.DriverInfoVo;
import com.gec.interest.model.vo.driver.DriverLoginVo;

public interface DriverInfoService extends IService<DriverInfo> {
    Long login(String code);
    DriverLoginVo getDriverLoginInfo(Long driverId);

    DriverAuthInfoVo getDriverAuthInfo(Long driverId);
    Boolean updateDriverAuthInfo(UpdateDriverAuthInfoForm updateDriverAuthInfoForm);
    Boolean creatDriverFaceModel(DriverFaceModelForm driverFaceModelForm);
    DriverSet getDriverSet(Long driverId);
    Boolean isFaceRecognition(Long driverId);
    Boolean verifyDriverFace(DriverFaceModelForm driverFaceModelForm);
    Boolean updateServiceStatus(Long driverId, Integer status);
    DriverInfoVo getDriverInfo(Long driverId);

    String getDriverOpenId(Long driverId);
}

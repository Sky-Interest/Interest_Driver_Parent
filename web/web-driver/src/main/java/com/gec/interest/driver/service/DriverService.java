package com.gec.interest.driver.service;

import com.gec.interest.model.form.driver.DriverFaceModelForm;
import com.gec.interest.model.form.driver.UpdateDriverAuthInfoForm;
import com.gec.interest.model.vo.driver.DriverAuthInfoVo;
import com.gec.interest.model.vo.driver.DriverLoginVo;

public interface DriverService {
    String login(String code);
    DriverLoginVo getDriverLoginInfo(Long driverId);
    DriverAuthInfoVo getDriverAuthInfo(Long driverId);
    Boolean updateDriverAuthInfo(UpdateDriverAuthInfoForm updateDriverAuthInfoForm);
    Boolean creatDriverFaceModel(DriverFaceModelForm driverFaceModelForm);
    Boolean isFaceRecognition(Long driverId);
    Boolean verifyDriverFace(DriverFaceModelForm driverFaceModelForm);
    Boolean startService(Long driverId);

}

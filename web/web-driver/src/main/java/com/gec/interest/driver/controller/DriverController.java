package com.gec.interest.driver.controller;

import com.gec.interest.common.login.InterestLogin;
import com.gec.interest.common.result.Result;
import com.gec.interest.common.util.AuthContextHolder;
import com.gec.interest.driver.service.DriverService;
import com.gec.interest.model.form.driver.DriverFaceModelForm;
import com.gec.interest.model.form.driver.UpdateDriverAuthInfoForm;
import com.gec.interest.model.vo.driver.DriverAuthInfoVo;
import com.gec.interest.model.vo.driver.DriverLoginVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "司机API接口管理")
@RestController
@RequestMapping(value="/driver")
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverController {
    @Autowired
    private DriverService driverService;

    @Operation(summary = "小程序授权登录")
    @GetMapping("/login/{code}")
    public Result<String> login(@PathVariable String code) {
        return Result.ok(driverService.login(code));
    }
    @Operation(summary = "获取司机登录信息")
    @InterestLogin
    @GetMapping("/getDriverLoginInfo")
    public Result<DriverLoginVo> getDriverLoginInfo() {
        Long driverId = AuthContextHolder.getUserId();
        return Result.ok(driverService.getDriverLoginInfo(driverId));
    }
    @Operation(summary = "获取司机认证信息")
    @InterestLogin
    @GetMapping("/getDriverAuthInfo")
    public Result<DriverAuthInfoVo> getDriverAuthInfo() {
        Long driverId = AuthContextHolder.getUserId();
        return Result.ok(driverService.getDriverAuthInfo(driverId));
    }
    @Operation(summary = "更新司机认证信息")
    @InterestLogin
    @PostMapping("/updateDriverAuthInfo")
    public Result<Boolean> updateDriverAuthInfo(@RequestBody UpdateDriverAuthInfoForm updateDriverAuthInfoForm) {
        updateDriverAuthInfoForm.setDriverId(AuthContextHolder.getUserId());
        return Result.ok(driverService.updateDriverAuthInfo(updateDriverAuthInfoForm));
    }
    @Operation(summary = "创建司机人脸模型")
    @InterestLogin
    @PostMapping("/creatDriverFaceModel")
    public Result<Boolean> creatDriverFaceModel(@RequestBody DriverFaceModelForm driverFaceModelForm) {
        driverFaceModelForm.setDriverId(AuthContextHolder.getUserId());
        return Result.ok(driverService.creatDriverFaceModel(driverFaceModelForm));
    }
    @Operation(summary = "判断司机当日是否进行过人脸识别")
    @InterestLogin
    @GetMapping("/isFaceRecognition")
    Result<Boolean> isFaceRecognition() {
        Long driverId = AuthContextHolder.getUserId();
        return Result.ok(driverService.isFaceRecognition(driverId));
    }

}


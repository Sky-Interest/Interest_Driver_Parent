package com.gec.interest.driver.service.impl;

import com.gec.interest.common.execption.interestException;
import com.gec.interest.common.result.ResultCodeEnum;
import com.gec.interest.driver.client.DriverInfoFeignClient;
import com.gec.interest.driver.service.LocationService;
import com.gec.interest.map.client.LocationFeignClient;
import com.gec.interest.model.entity.driver.DriverSet;
import com.gec.interest.model.form.map.OrderServiceLocationForm;
import com.gec.interest.model.form.map.UpdateDriverLocationForm;
import com.gec.interest.model.form.map.UpdateOrderLocationForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class LocationServiceImpl implements LocationService {
    @Autowired
    LocationFeignClient locationFeignClient;
    @Autowired
    DriverInfoFeignClient driverInfoFeignClient;

    //司机端-web-位置更新的远程调度
    @Override
    public Boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm) {
        //根据司机id查询set信息
        DriverSet driverSet = driverInfoFeignClient.getDriverSet(updateDriverLocationForm.getDriverId()).getData();
        //判断当前的司机是否开启了接单
        if (driverSet != null && driverSet.getServiceStatus().intValue() == 1) {//开启了接单
            return locationFeignClient.updateDriverLocation(updateDriverLocationForm).getData();
        } else {//抛出异常
            throw new interestException(ResultCodeEnum.NO_START_SERVICE);
        }

    }
    @Override
    public Boolean updateOrderLocationToCache(UpdateOrderLocationForm updateOrderLocationForm) {
        return locationFeignClient.updateOrderLocationToCache(updateOrderLocationForm).getData();
    }
    @Override
    public Boolean saveOrderServiceLocation(List<OrderServiceLocationForm> orderLocationServiceFormList) {
        return locationFeignClient.saveOrderServiceLocation(orderLocationServiceFormList).getData();
    }



}

package com.gec.interest.driver.service.impl;

import com.gec.interest.driver.service.LocationService;
import com.gec.interest.map.client.LocationFeignClient;
import com.gec.interest.model.form.map.UpdateDriverLocationForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class LocationServiceImpl implements LocationService {
    @Autowired
    LocationFeignClient locationFeignClient;

    @Override
    public Boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm) {
        return locationFeignClient.updateDriverLocation(updateDriverLocationForm).getData();
    }

}

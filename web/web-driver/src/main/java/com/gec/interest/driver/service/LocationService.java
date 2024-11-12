package com.gec.interest.driver.service;

import com.gec.interest.model.form.map.OrderServiceLocationForm;
import com.gec.interest.model.form.map.UpdateDriverLocationForm;
import com.gec.interest.model.form.map.UpdateOrderLocationForm;

import java.util.List;

public interface LocationService {
    Boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm);
    Boolean updateOrderLocationToCache(UpdateOrderLocationForm updateOrderLocationForm);
    Boolean saveOrderServiceLocation(List<OrderServiceLocationForm> orderLocationServiceFormList);


}

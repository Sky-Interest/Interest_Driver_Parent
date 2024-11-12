package com.gec.interest.map.service;

import com.gec.interest.model.form.map.OrderServiceLocationForm;
import com.gec.interest.model.form.map.SearchNearByDriverForm;
import com.gec.interest.model.form.map.UpdateDriverLocationForm;
import com.gec.interest.model.form.map.UpdateOrderLocationForm;
import com.gec.interest.model.vo.map.NearByDriverVo;
import com.gec.interest.model.vo.map.OrderLocationVo;
import com.gec.interest.model.vo.map.OrderServiceLastLocationVo;

import java.util.List;

public interface LocationService {
    Boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm);

    Boolean removeDriverLocation(Long driverId);
    List<NearByDriverVo> searchNearByDriver(SearchNearByDriverForm searchNearByDriverForm);
    Boolean updateOrderLocationToCache(UpdateOrderLocationForm updateOrderLocationForm);
    OrderLocationVo getCacheOrderLocation(Long orderId);
    Boolean saveOrderServiceLocation(List<OrderServiceLocationForm> orderLocationServiceFormList);
    OrderServiceLastLocationVo getOrderServiceLastLocation(Long orderId);

}

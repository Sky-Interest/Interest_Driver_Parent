package com.gec.interest.map.service;

import com.gec.interest.model.form.map.SearchNearByDriverForm;
import com.gec.interest.model.form.map.UpdateDriverLocationForm;
import com.gec.interest.model.vo.map.NearByDriverVo;

import java.util.List;

public interface LocationService {
    Boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm);

    Boolean removeDriverLocation(Long driverId);
    List<NearByDriverVo> searchNearByDriver(SearchNearByDriverForm searchNearByDriverForm);
}

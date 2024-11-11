package com.gec.interest.driver.service;

import com.gec.interest.model.form.map.UpdateDriverLocationForm;
import com.gec.interest.model.form.map.UpdateOrderLocationForm;

public interface LocationService {
    Boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm);
    Boolean updateOrderLocationToCache(UpdateOrderLocationForm updateOrderLocationForm);

}

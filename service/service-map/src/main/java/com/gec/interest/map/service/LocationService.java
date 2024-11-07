package com.gec.interest.map.service;

import com.gec.interest.model.form.map.UpdateDriverLocationForm;

public interface LocationService {
    Boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm);

    Boolean removeDriverLocation(Long driverId);
}

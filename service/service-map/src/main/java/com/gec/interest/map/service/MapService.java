package com.gec.interest.map.service;

import com.gec.interest.model.form.map.CalculateDrivingLineForm;
import com.gec.interest.model.vo.map.DrivingLineVo;

public interface MapService {
    DrivingLineVo calculateDrivingLine(CalculateDrivingLineForm calculateDrivingLineForm);
}

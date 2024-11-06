package com.gec.interest.map.client;

import com.gec.interest.common.result.Result;
import com.gec.interest.model.form.map.CalculateDrivingLineForm;
import com.gec.interest.model.vo.map.DrivingLineVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-map")
public interface MapFeignClient {

    /**
     * 计算驾驶线路
     *
     * @param calculateDrivingLineForm
     * @return
     */
    @PostMapping("/map/calculateDrivingLine")
    Result<DrivingLineVo> calculateDrivingLine(@RequestBody CalculateDrivingLineForm calculateDrivingLineForm);


}

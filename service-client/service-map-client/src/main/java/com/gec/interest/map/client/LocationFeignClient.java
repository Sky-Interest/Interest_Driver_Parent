package com.gec.interest.map.client;

import com.gec.interest.common.result.Result;
import com.gec.interest.model.form.map.UpdateDriverLocationForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-map")
public interface LocationFeignClient {
    /**
     * 开启接单服务：更新司机经纬度位置
     * @param updateDriverLocationForm
     * @return
     */
    @PostMapping("/map/location/updateDriverLocation")
    Result<Boolean> updateDriverLocation(@RequestBody UpdateDriverLocationForm updateDriverLocationForm);
    /**
     * 关闭接单服务：删除司机经纬度位置
     * @param driverId
     * @return
     */
    @DeleteMapping("/map/location/removeDriverLocation/{driverId}")
    Result<Boolean> removeDriverLocation(@PathVariable("driverId") Long driverId);

}

package com.gec.interest.map.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "service-map")
public interface LocationFeignClient {


}

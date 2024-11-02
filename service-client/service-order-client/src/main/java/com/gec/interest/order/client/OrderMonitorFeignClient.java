package com.gec.interest.order.client;

import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(value = "service-order")
public interface OrderMonitorFeignClient {


}

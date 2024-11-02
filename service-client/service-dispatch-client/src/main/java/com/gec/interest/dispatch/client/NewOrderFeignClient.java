package com.gec.interest.dispatch.client;

import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(value = "service-dispatch")
public interface NewOrderFeignClient {


}

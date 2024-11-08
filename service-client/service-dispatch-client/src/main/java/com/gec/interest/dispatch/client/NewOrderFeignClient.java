package com.gec.interest.dispatch.client;

import com.gec.interest.common.result.Result;
import com.gec.interest.model.vo.dispatch.NewOrderTaskVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(value = "service-dispatch")
public interface NewOrderFeignClient {
    /**
     * 添加新订单任务
     * @param newOrderDispatchVo
     * @return
     */
    @PostMapping("/dispatch/newOrder/addAndStartTask")
    Result<Long> addAndStartTask(@RequestBody NewOrderTaskVo newOrderDispatchVo);
}

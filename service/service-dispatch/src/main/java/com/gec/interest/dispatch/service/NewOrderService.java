package com.gec.interest.dispatch.service;

import com.gec.interest.model.vo.dispatch.NewOrderTaskVo;
import com.gec.interest.model.vo.order.NewOrderDataVo;

import java.util.List;

public interface NewOrderService {
    Long addAndStartTask(NewOrderTaskVo newOrderTaskVo);
    Boolean executeTask(Long jobId);
    List<NewOrderDataVo> findNewOrderQueueData(Long driverId);

    Boolean clearNewOrderQueueData(Long driverId);
}

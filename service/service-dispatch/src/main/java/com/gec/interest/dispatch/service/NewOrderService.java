package com.gec.interest.dispatch.service;

import com.gec.interest.model.vo.dispatch.NewOrderTaskVo;

public interface NewOrderService {
    Long addAndStartTask(NewOrderTaskVo newOrderTaskVo);
    Boolean executeTask(Long jobId);
}

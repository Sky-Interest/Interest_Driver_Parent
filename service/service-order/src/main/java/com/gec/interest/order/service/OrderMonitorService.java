package com.gec.interest.order.service;

import com.gec.interest.model.entity.order.OrderMonitor;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.interest.model.entity.order.OrderMonitorRecord;

public interface OrderMonitorService extends IService<OrderMonitor> {
    Boolean saveOrderMonitorRecord(OrderMonitorRecord orderMonitorRecord);

}

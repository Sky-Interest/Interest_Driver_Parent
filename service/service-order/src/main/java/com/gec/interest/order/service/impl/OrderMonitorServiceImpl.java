package com.gec.interest.order.service.impl;

import com.gec.interest.model.entity.order.OrderMonitor;
import com.gec.interest.model.entity.order.OrderMonitorRecord;
import com.gec.interest.order.mapper.OrderMonitorMapper;
import com.gec.interest.order.repository.OrderMonitorRecordRepository;
import com.gec.interest.order.service.OrderMonitorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OrderMonitorServiceImpl extends ServiceImpl<OrderMonitorMapper, OrderMonitor> implements OrderMonitorService {
    @Autowired
    private OrderMonitorRecordRepository orderMonitorRecordRepository;

    @Override
    public Boolean saveOrderMonitorRecord(OrderMonitorRecord orderMonitorRecord) {
        orderMonitorRecordRepository.save(orderMonitorRecord);
        return true;
    }


}

package com.gec.interest.driver.service.impl;

import com.gec.interest.driver.service.FileService;
import com.gec.interest.driver.service.MonitorService;
import com.gec.interest.model.entity.order.OrderMonitorRecord;
import com.gec.interest.model.form.order.OrderMonitorForm;
import com.gec.interest.order.client.OrderMonitorFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class MonitorServiceImpl implements MonitorService {
    @Autowired
    private FileService fileService;

    @Autowired
    private OrderMonitorFeignClient orderMonitorFeignClient;

    @Override
    public Boolean upload(MultipartFile file, OrderMonitorForm orderMonitorForm) {
        //上传对话文件
        String url = fileService.upload(file);
        log.info("upload: {}", url);

        //保存订单监控记录数
        OrderMonitorRecord orderMonitorRecord = new OrderMonitorRecord();
        orderMonitorRecord.setOrderId(orderMonitorForm.getOrderId());
        orderMonitorRecord.setFileUrl(url);
        orderMonitorRecord.setContent(orderMonitorForm.getContent());
        orderMonitorFeignClient.saveMonitorRecord(orderMonitorRecord);
        return true;
    }


}

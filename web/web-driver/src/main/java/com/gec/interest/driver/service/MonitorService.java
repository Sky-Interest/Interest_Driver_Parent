package com.gec.interest.driver.service;

import com.gec.interest.model.form.order.OrderMonitorForm;
import org.springframework.web.multipart.MultipartFile;

public interface MonitorService {
    Boolean upload(MultipartFile file, OrderMonitorForm orderMonitorForm);


}

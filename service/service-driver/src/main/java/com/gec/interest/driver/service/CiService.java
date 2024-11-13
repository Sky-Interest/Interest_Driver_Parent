package com.gec.interest.driver.service;

import com.gec.interest.model.vo.order.TextAuditingVo;

public interface CiService {
    Boolean imageAuditing(String path);
    TextAuditingVo textAuditing(String content);



}

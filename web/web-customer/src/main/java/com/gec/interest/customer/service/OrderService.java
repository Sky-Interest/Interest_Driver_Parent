package com.gec.interest.customer.service;

import com.gec.interest.model.form.customer.ExpectOrderForm;
import com.gec.interest.model.vo.customer.ExpectOrderVo;

public interface OrderService {
    ExpectOrderVo expectOrder(ExpectOrderForm expectOrderForm);
}

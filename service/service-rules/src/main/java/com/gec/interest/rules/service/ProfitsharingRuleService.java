package com.gec.interest.rules.service;

import com.gec.interest.model.form.rules.ProfitsharingRuleRequestForm;
import com.gec.interest.model.vo.rules.ProfitsharingRuleResponseVo;

public interface ProfitsharingRuleService {

    ProfitsharingRuleResponseVo calculateOrderProfitsharingFee(ProfitsharingRuleRequestForm profitsharingRuleRequestForm);
}

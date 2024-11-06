package com.gec.interest.rules.service;

import com.gec.interest.model.form.rules.FeeRuleRequestForm;
import com.gec.interest.model.vo.rules.FeeRuleResponseVo;

public interface FeeRuleService {
    FeeRuleResponseVo calculateOrderFee(FeeRuleRequestForm calculateOrderFeeForm);
}

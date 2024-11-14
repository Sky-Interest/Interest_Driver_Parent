package com.gec.interest.rules.service;

import com.gec.interest.model.form.rules.RewardRuleRequestForm;
import com.gec.interest.model.vo.rules.RewardRuleResponseVo;

public interface RewardRuleService {
    RewardRuleResponseVo
    calculateOrderRewardFee(RewardRuleRequestForm rewardRuleRequestForm);

}

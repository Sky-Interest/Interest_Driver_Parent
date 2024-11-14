package com.gec.interest.rules.client;

import com.gec.interest.common.result.Result;
import com.gec.interest.model.form.rules.RewardRuleRequestForm;
import com.gec.interest.model.vo.rules.RewardRuleResponseVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "service-rules")
public interface RewardRuleFeignClient {
    /**
     * 计算订单奖励费用
     * @param rewardRuleRequestForm
     * @return
     */
    @PostMapping("/rules/reward/calculateOrderRewardFee")
    Result<RewardRuleResponseVo> calculateOrderRewardFee(@RequestBody RewardRuleRequestForm rewardRuleRequestForm);

}

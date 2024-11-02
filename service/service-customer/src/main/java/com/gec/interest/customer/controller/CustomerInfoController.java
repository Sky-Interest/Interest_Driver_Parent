package com.gec.interest.customer.controller;

import com.gec.interest.common.result.Result;
import com.gec.interest.customer.service.CustomerInfoService;
import com.gec.interest.model.entity.customer.CustomerInfo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/customer/info")
@SuppressWarnings({"unchecked", "rawtypes"})
public class CustomerInfoController {

	@Autowired
	private CustomerInfoService customerInfoService;

	@Operation(summary = "获取客户基本信息")
	@GetMapping("/getCustomerInfo/{customerId}")
	public Result<CustomerInfo> getCustomerInfo(@PathVariable Long customerId) {
		return Result.ok(customerInfoService.getById(customerId));
	}
}


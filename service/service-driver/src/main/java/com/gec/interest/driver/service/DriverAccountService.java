package com.gec.interest.driver.service;

import com.gec.interest.model.entity.driver.DriverAccount;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gec.interest.model.form.driver.TransferForm;

public interface DriverAccountService extends IService<DriverAccount> {


    Boolean transfer(TransferForm transferForm);
}

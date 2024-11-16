package com.gec.interest.driver.mapper;

import com.gec.interest.model.entity.driver.DriverAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

@Mapper
public interface DriverAccountMapper extends BaseMapper<DriverAccount> {


    Integer add(@Param("driverId") Long userId, @Param("amount") BigDecimal amount);

}

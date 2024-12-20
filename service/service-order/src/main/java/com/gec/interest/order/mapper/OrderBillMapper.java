package com.gec.interest.order.mapper;

import com.gec.interest.model.entity.order.OrderBill;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

@Mapper
public interface OrderBillMapper extends BaseMapper<OrderBill> {

    int updateCouponAmount(@Param("orderId") Long orderId, @Param("couponAmount") BigDecimal couponAmount);

}

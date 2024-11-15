package com.gec.interest.order.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.interest.model.entity.order.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gec.interest.model.vo.order.OrderListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    IPage<OrderListVo> selectCustomerOrderPage(Page<OrderInfo> page, @Param("customerId") Long customerId);

    IPage<OrderListVo> selectDriverOrderPage(Page<OrderInfo> page, @Param("driverId") Long driverId);

}

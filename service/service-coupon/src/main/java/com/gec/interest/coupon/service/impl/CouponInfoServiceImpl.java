package com.gec.interest.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.interest.common.execption.interestException;
import com.gec.interest.common.result.ResultCodeEnum;
import com.gec.interest.coupon.mapper.CouponInfoMapper;
import com.gec.interest.coupon.mapper.CustomerCouponMapper;
import com.gec.interest.coupon.service.CouponInfoService;
import com.gec.interest.model.entity.coupon.CouponInfo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.interest.model.entity.coupon.CustomerCoupon;
import com.gec.interest.model.vo.base.PageVo;
import com.gec.interest.model.vo.coupon.NoReceiveCouponVo;
import com.gec.interest.model.vo.coupon.NoUseCouponVo;
import com.gec.interest.model.vo.coupon.UsedCouponVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CouponInfoServiceImpl extends ServiceImpl<CouponInfoMapper, CouponInfo> implements CouponInfoService {

    @Autowired
    private CouponInfoMapper couponInfoMapper;
    @Autowired
    private CustomerCouponMapper customerCouponMapper;

    @Override
    public PageVo<NoReceiveCouponVo> findNoReceivePage(Page<CouponInfo> pageParam, Long customerId) {
        IPage<NoReceiveCouponVo> pageInfo = couponInfoMapper.findNoReceivePage(pageParam, customerId);
        return new PageVo(pageInfo.getRecords(), pageInfo.getPages(), pageInfo.getTotal());
    }
    @Override
    public PageVo<NoUseCouponVo> findNoUsePage(Page<CouponInfo> pageParam, Long customerId) {
        IPage<NoUseCouponVo> pageInfo = couponInfoMapper.findNoUsePage(pageParam, customerId);
        return new PageVo(pageInfo.getRecords(), pageInfo.getPages(), pageInfo.getTotal());
    }
    @Override
    public PageVo<UsedCouponVo> findUsedPage(Page<CouponInfo> pageParam, Long customerId) {
        IPage<UsedCouponVo> pageInfo = couponInfoMapper.findUsedPage(pageParam, customerId);
        return new PageVo(pageInfo.getRecords(), pageInfo.getPages(), pageInfo.getTotal());
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean receive(Long customerId, Long couponId) {
        //1、查询优惠券
        CouponInfo couponInfo = this.getById(couponId);
        if(null == couponInfo) {
            throw new interestException(ResultCodeEnum.DATA_ERROR);
        }

        //2、优惠券过期日期判断
        if (couponInfo.getExpireTime().before(new Date())) {
            throw new interestException(ResultCodeEnum.COUPON_EXPIRE);
        }

        //3、校验库存，优惠券领取数量判断
        if (couponInfo.getPublishCount() !=0 && couponInfo.getReceiveCount() >= couponInfo.getPublishCount()) {
            throw new interestException(ResultCodeEnum.COUPON_LESS);
        }

        //4、校验每人限领数量
        if (couponInfo.getPerLimit() > 0) {//判断是否有领取数量
            //4.1、统计当前用户对当前优惠券的已经领取的数量
            long count = customerCouponMapper.selectCount(new LambdaQueryWrapper<CustomerCoupon>()
                    .eq(CustomerCoupon::getCouponId, couponId)
                    .eq(CustomerCoupon::getCustomerId, customerId));
            //4.2、校验限领数量【超过限领】
            if (count >= couponInfo.getPerLimit()) {
                throw new interestException(ResultCodeEnum.COUPON_USER_LIMIT);
            }
        }

        //5、更新优惠券领取数量
        int row = couponInfoMapper.updateReceiveCount(couponId);
        if (row == 1) {
            //6、保存领取记录
            this.saveCustomerCoupon(customerId, couponId, couponInfo.getExpireTime());
            return true;
        }
        throw new interestException(ResultCodeEnum.COUPON_LESS);
    }

    private void saveCustomerCoupon(Long customerId, Long couponId, Date expireTime) {
        CustomerCoupon customerCoupon = new CustomerCoupon();
        customerCoupon.setCustomerId(customerId);
        customerCoupon.setCouponId(couponId);
        customerCoupon.setStatus(1);
        customerCoupon.setReceiveTime(new Date());
        customerCoupon.setExpireTime(expireTime);
        customerCouponMapper.insert(customerCoupon);
    }



}

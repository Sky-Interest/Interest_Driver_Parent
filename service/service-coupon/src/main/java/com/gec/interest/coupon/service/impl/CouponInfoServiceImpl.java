package com.gec.interest.coupon.service.impl;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.interest.common.constant.RedisConstant;
import com.gec.interest.common.execption.interestException;
import com.gec.interest.common.result.ResultCodeEnum;
import com.gec.interest.coupon.mapper.CouponInfoMapper;
import com.gec.interest.coupon.mapper.CustomerCouponMapper;
import com.gec.interest.coupon.service.CouponInfoService;
import com.gec.interest.model.entity.coupon.CouponInfo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.interest.model.entity.coupon.CustomerCoupon;
import com.gec.interest.model.form.coupon.UseCouponForm;
import com.gec.interest.model.vo.base.PageVo;
import com.gec.interest.model.vo.coupon.AvailableCouponVo;
import com.gec.interest.model.vo.coupon.NoReceiveCouponVo;
import com.gec.interest.model.vo.coupon.NoUseCouponVo;
import com.gec.interest.model.vo.coupon.UsedCouponVo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CouponInfoServiceImpl extends ServiceImpl<CouponInfoMapper, CouponInfo> implements CouponInfoService {

    @Autowired
    private CouponInfoMapper couponInfoMapper;
    @Autowired
    private CustomerCouponMapper customerCouponMapper;
    @Autowired
    private RedissonClient redissonClient;

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

        RLock lock = null;
        try {
            // 初始化分布式锁
            //每人领取限制  与 优惠券发行总数 必须保证原子性，使用customerId减少锁的粒度，增加并发能力
            lock = redissonClient.getLock(RedisConstant.COUPON_LOCK + customerId);
            boolean flag = lock.tryLock(RedisConstant.COUPON_LOCK_WAIT_TIME, RedisConstant.COUPON_LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (flag) {
                //4、校验每人限领数量
                if (couponInfo.getPerLimit() > 0) {
                    //4.1、统计当前用户对当前优惠券的已经领取的数量
                    long count = customerCouponMapper.selectCount(new LambdaQueryWrapper<CustomerCoupon>().eq(CustomerCoupon::getCouponId, couponId).eq(CustomerCoupon::getCustomerId, customerId));
                    //4.2、校验限领数量
                    if (count >= couponInfo.getPerLimit()) {
                        throw new interestException(ResultCodeEnum.COUPON_USER_LIMIT);
                    }
                }

                //5、更新优惠券领取数量
                int row = 0;
                if (couponInfo.getPublishCount() == 0) {//没有限制
                    row = couponInfoMapper.updateReceiveCount(couponId);
                } else {
                    row = couponInfoMapper.updateReceiveCountByLimit(couponId);
                }
                if (row == 1) {
                    //6、保存领取记录
                    this.saveCustomerCoupon(customerId, couponId, couponInfo.getExpireTime());
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != lock) {
                lock.unlock();
            }
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
    @Override
    public List<AvailableCouponVo> findAvailableCoupon(Long customerId, BigDecimal orderAmount) {
        //1、可使用的优惠卷集合定义
        List<AvailableCouponVo> availableCouponVoList = new ArrayList<>();
        //2、获取未使用的优惠卷列表
        List<NoUseCouponVo> list = couponInfoMapper.findNoUseList(customerId);
        //3、现金-列表
        List<NoUseCouponVo> type1List = list.stream().filter(item -> item.getCouponType().intValue() == 1).collect(Collectors.toList());
        //逻辑判断、计算优惠价格[存放list中]
        for (NoUseCouponVo noUseCouponVo : type1List) {
            //获取优惠卷减免金额【现金卷】
            BigDecimal reduceAmount = noUseCouponVo.getAmount();
            //1、没门槛  订单金额要大于优惠卷的优惠的金额
            if (noUseCouponVo.getConditionAmount().doubleValue() == 0 && orderAmount.subtract(reduceAmount).doubleValue() >= 0) {
                availableCouponVoList.add(this.buildBestNoUseCouponVo(noUseCouponVo, reduceAmount));
            }
            //2、有门槛  大于门槛金额
            if (noUseCouponVo.getConditionAmount().doubleValue() > 0 && orderAmount.subtract(noUseCouponVo.getConditionAmount()).doubleValue() >= 0) {
                availableCouponVoList.add(this.buildBestNoUseCouponVo(noUseCouponVo, reduceAmount));
            }
        }
        //4、折扣-列表
        List<NoUseCouponVo> type2List = list.stream().filter(item -> item.getCouponType().intValue() == 2).collect(Collectors.toList());
        //逻辑判断、计算优惠价格[存放list中]
        for (NoUseCouponVo noUseCouponVo : type2List) {
            //获取优惠卷减免金额【折扣卷】[设置2位数的进度处理]
            BigDecimal reduceAmount = orderAmount
                    .multiply(noUseCouponVo.getDiscount())
                    .divide(new BigDecimal("10"))
                    .setScale(2, RoundingMode.HALF_UP);
            //1、没门槛  订单金额要大于优惠卷的优惠的金额
            if (noUseCouponVo.getConditionAmount().doubleValue() == 0) {
                availableCouponVoList.add(this.buildBestNoUseCouponVo(noUseCouponVo, reduceAmount));
            }
            //2、有门槛  大于门槛金额
            if (noUseCouponVo.getConditionAmount().doubleValue() > 0 && orderAmount.subtract(noUseCouponVo.getConditionAmount()).doubleValue() >= 0) {
                availableCouponVoList.add(this.buildBestNoUseCouponVo(noUseCouponVo, reduceAmount));
            }
        }
        //5、排序操作
        if (!CollectionUtils.isEmpty(availableCouponVoList)) {
            Collections.sort(availableCouponVoList, (o1, o2) -> o1.getReduceAmount().compareTo(o2.getReduceAmount()));
        }
        //返回
        return availableCouponVoList;
    }

    private AvailableCouponVo buildBestNoUseCouponVo(NoUseCouponVo noUseCouponVo, BigDecimal reduceAmount) {
        AvailableCouponVo bestNoUseCouponVo = new AvailableCouponVo();
        BeanUtils.copyProperties(noUseCouponVo, bestNoUseCouponVo);
        bestNoUseCouponVo.setCouponId(noUseCouponVo.getId());
        bestNoUseCouponVo.setReduceAmount(reduceAmount);
        return bestNoUseCouponVo;
    }
    @Override
    @Transactional(noRollbackFor = Exception.class)
    public BigDecimal useCoupon(UseCouponForm useCouponForm) {
        //1、获取使用的优惠卷信息
        CustomerCoupon customerCoupon = customerCouponMapper.selectById(useCouponForm.getCustomerCouponId());
        if (customerCoupon == null) {
            throw new interestException(ResultCodeEnum.ARGUMENT_VALID_ERROR);
        }
        //2、判断优惠卷是否该乘客拥有
        CouponInfo couponInfo = couponInfoMapper.selectById(customerCoupon.getCouponId());
        if (couponInfo == null) {
            throw new interestException(ResultCodeEnum.ARGUMENT_VALID_ERROR);
        }
        if (customerCoupon.getCustomerId().intValue() != useCouponForm.getCustomerId().intValue()) {
            throw new interestException(ResultCodeEnum.ILLEGAL_REQUEST);
        }
        //3、获取优惠金额
        BigDecimal reduceAmount = null;
        if (couponInfo.getCouponType().intValue() == 1) {//现金卷
            reduceAmount = couponInfo.getAmount();
        } else {//现金卷折扣卷
            //获取优惠卷减免金额【折扣卷】[设置2位数的进度处理]
            reduceAmount = useCouponForm.getOrderAmount()
                    .multiply(couponInfo.getDiscount())
                    .divide(new BigDecimal("10"))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        //4、合理化判断优惠金额是否大于0、更新使用信息
        if (reduceAmount.doubleValue() > 0) {
            //更新当前使用的优惠卷的【已使用数量】
            int update = couponInfoMapper.updateUseCount(couponInfo.getId());
            //更新customer_coupon-优化券状态（1：未使用 2：已使用）使用时间
            if (update > 0) {
                CustomerCoupon updateCustomerCoupon = new CustomerCoupon();
                updateCustomerCoupon.setId(customerCoupon.getId());
                updateCustomerCoupon.setUsedTime(new Date());
                updateCustomerCoupon.setOrderId(useCouponForm.getOrderId());
                //更新操作
                customerCouponMapper.updateById(updateCustomerCoupon);
                //返回
                return reduceAmount;
            }
        }
        throw new interestException(ResultCodeEnum.DATA_ERROR);
    }


}

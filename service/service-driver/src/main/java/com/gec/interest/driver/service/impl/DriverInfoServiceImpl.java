package com.gec.interest.driver.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gec.interest.common.constant.SystemConstant;
import com.gec.interest.common.execption.interestException;
import com.gec.interest.common.result.ResultCodeEnum;
import com.gec.interest.driver.mapper.DriverAccountMapper;
import com.gec.interest.driver.mapper.DriverInfoMapper;
import com.gec.interest.driver.mapper.DriverLoginLogMapper;
import com.gec.interest.driver.mapper.DriverSetMapper;
import com.gec.interest.driver.service.CosService;
import com.gec.interest.driver.service.DriverInfoService;
import com.gec.interest.model.entity.driver.DriverAccount;
import com.gec.interest.model.entity.driver.DriverInfo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gec.interest.model.entity.driver.DriverLoginLog;
import com.gec.interest.model.entity.driver.DriverSet;
import com.gec.interest.model.form.driver.UpdateDriverAuthInfoForm;
import com.gec.interest.model.vo.driver.DriverAuthInfoVo;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverInfoServiceImpl extends ServiceImpl<DriverInfoMapper, DriverInfo> implements DriverInfoService {
    @Autowired
    private WxMaService wxMaService;

    @Autowired
    private DriverInfoMapper driverInfoMapper;

    @Autowired
    private DriverSetMapper driverSetMapper;

    @Autowired
    private DriverAccountMapper driverAccountMapper;

    @Autowired
    private DriverLoginLogMapper driverLoginLogMapper;

    //小程序授权登录
    @Override
    public Long login(String code) {
        try {
            //根据code + 小程序id + 秘钥请求微信接口，返回openid
            WxMaJscode2SessionResult sessionInfo =
                    wxMaService.getUserService().getSessionInfo(code);
            String openid = sessionInfo.getOpenid();

            //根据openid查询是否第一次登录
            LambdaQueryWrapper<DriverInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DriverInfo::getWxOpenId,openid);
            DriverInfo driverInfo = driverInfoMapper.selectOne(wrapper);

            if(driverInfo == null) {
                //添加司机基本信息
                driverInfo = new DriverInfo();
                driverInfo.setNickname(String.valueOf(System.currentTimeMillis()));
                driverInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
                driverInfo.setWxOpenId(openid);
                driverInfoMapper.insert(driverInfo);

                //初始化司机设置
                DriverSet driverSet = new DriverSet();
                driverSet.setDriverId(driverInfo.getId());
                driverSet.setOrderDistance(new BigDecimal(0));//0：无限制
                driverSet.setAcceptDistance(new BigDecimal(SystemConstant.ACCEPT_DISTANCE));//默认接单范围：5公里
                driverSet.setIsAutoAccept(0);//0：否 1：是
                driverSetMapper.insert(driverSet);

                //初始化司机账户信息
                DriverAccount driverAccount = new DriverAccount();
                driverAccount.setDriverId(driverInfo.getId());
                driverAccountMapper.insert(driverAccount);
            }

            //记录司机登录信息
            DriverLoginLog driverLoginLog = new DriverLoginLog();
            driverLoginLog.setDriverId(driverInfo.getId());
            driverLoginLog.setMsg("小程序登录");
            driverLoginLogMapper.insert(driverLoginLog);

            //返回司机id
            return driverInfo.getId();
        } catch (WxErrorException e) {
            throw new interestException(ResultCodeEnum.DATA_ERROR);
        }
    }
    @Autowired
    private CosService cosService;

    @Override
    public DriverAuthInfoVo getDriverAuthInfo(Long driverId) {
        DriverInfo driverInfo = this.getById(driverId);
        DriverAuthInfoVo driverAuthInfoVo = new DriverAuthInfoVo();
        BeanUtils.copyProperties(driverInfo, driverAuthInfoVo);
        driverAuthInfoVo.setIdcardBackShowUrl(cosService.getImageUrl(driverAuthInfoVo.getIdcardBackUrl()));
        driverAuthInfoVo.setIdcardFrontShowUrl(cosService.getImageUrl(driverAuthInfoVo.getIdcardFrontUrl()));
        driverAuthInfoVo.setIdcardHandShowUrl(cosService.getImageUrl(driverAuthInfoVo.getIdcardHandUrl()));
        driverAuthInfoVo.setDriverLicenseFrontShowUrl(cosService.getImageUrl(driverAuthInfoVo.getDriverLicenseFrontUrl()));
        driverAuthInfoVo.setDriverLicenseBackShowUrl(cosService.getImageUrl(driverAuthInfoVo.getDriverLicenseBackUrl()));
        driverAuthInfoVo.setDriverLicenseHandShowUrl(cosService.getImageUrl(driverAuthInfoVo.getDriverLicenseHandUrl()));
        return driverAuthInfoVo;
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean updateDriverAuthInfo(UpdateDriverAuthInfoForm updateDriverAuthInfoForm) {
        DriverInfo driverInfo = new DriverInfo();
        driverInfo.setId(updateDriverAuthInfoForm.getDriverId());
        BeanUtils.copyProperties(updateDriverAuthInfoForm, driverInfo);
        //修改司机的验证状态
        driverInfo.setAuthStatus(2);
        return this.updateById(driverInfo);
    }


}

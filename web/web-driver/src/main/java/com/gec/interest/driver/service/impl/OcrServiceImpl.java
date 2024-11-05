package com.gec.interest.driver.service.impl;

import com.gec.interest.driver.client.OcrFeignClient;
import com.gec.interest.driver.service.OcrService;
import com.gec.interest.model.vo.driver.DriverLicenseOcrVo;
import com.gec.interest.model.vo.driver.IdCardOcrVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OcrServiceImpl implements OcrService {
    @Autowired
    private OcrFeignClient ocrFeignClient;

    @Override
    public IdCardOcrVo idCardOcr(MultipartFile file) {
        return ocrFeignClient.idCardOcr(file).getData();
    }
    @Override
    public DriverLicenseOcrVo driverLicenseOcr(MultipartFile file) {
        return ocrFeignClient.driverLicenseOcr(file).getData();
    }

}

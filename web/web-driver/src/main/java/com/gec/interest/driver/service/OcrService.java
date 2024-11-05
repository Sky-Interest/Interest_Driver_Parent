package com.gec.interest.driver.service;

import com.gec.interest.model.vo.driver.DriverLicenseOcrVo;
import com.gec.interest.model.vo.driver.IdCardOcrVo;
import org.springframework.web.multipart.MultipartFile;

public interface OcrService {
    IdCardOcrVo idCardOcr(MultipartFile file);
    DriverLicenseOcrVo driverLicenseOcr(MultipartFile file);
}

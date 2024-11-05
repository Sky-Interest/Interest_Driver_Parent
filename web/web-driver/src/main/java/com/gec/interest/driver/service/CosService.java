package com.gec.interest.driver.service;

import com.gec.interest.model.vo.driver.CosUploadVo;
import org.springframework.web.multipart.MultipartFile;

public interface CosService {
    CosUploadVo upload(MultipartFile file, String path);

}

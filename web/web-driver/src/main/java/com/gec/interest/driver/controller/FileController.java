package com.gec.interest.driver.controller;

import com.gec.interest.common.result.Result;
import com.gec.interest.driver.service.FileService;
import com.gec.interest.model.vo.driver.CosUploadVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "上传管理接口")
@RestController
@RequestMapping("file")
public class FileController {

    @Autowired
    FileService fileService;

    @Operation(summary = "上传")
    @PostMapping("/upload")
    public Result<CosUploadVo> upload(@RequestPart("file") MultipartFile file
            , @RequestParam(name = "path", defaultValue = "car", required = false) String path) {
        return Result.ok(fileService.upload(file, path));
    }

}

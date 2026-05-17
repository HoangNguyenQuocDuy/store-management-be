package com.seveneleven.controller;

import com.seveneleven.dto.ApiResponse;
import com.seveneleven.service.S3UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {

    private final S3UploadService s3UploadService;

    @PostMapping("/image")
    public ApiResponse<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("#UploadController.uploadImage - START");

        String url = s3UploadService.uploadImage(file);
        return ApiResponse.ok(Map.of("url", url));
    }
}

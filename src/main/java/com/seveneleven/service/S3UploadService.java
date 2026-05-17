package com.seveneleven.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3UploadService {

    String uploadImage(MultipartFile file);

    void deleteImage(String imageUrl);

}

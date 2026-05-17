package com.seveneleven.service.impl;

import com.seveneleven.config.AWSProperties;
import com.seveneleven.exception.BadRequestException;
import com.seveneleven.service.S3UploadService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadServiceImpl implements S3UploadService {

    private final S3Client s3Client;
    private final AWSProperties awsProperties;

    @Override
    public String uploadImage(MultipartFile file) {
        log.error("#S3UploadServiceImpl.uploadImage - START");

        if (!awsProperties.isEnabled() || s3Client == null) {
            log.error("#S3UploadServiceImpl.uploadImage - ERROR - S3 service is disabled or S3Client bean is missing");
            throw new RuntimeException("Missing S3 AWS credentials or feature is disabled!");
        }

        if (file.isEmpty()) {
            throw new RuntimeException("Empty file!");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".jpg";
        String key = "products/" + UUID.randomUUID() + ext;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(awsProperties.getS3().getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String url = String.format("https://%s.s3.%s.amazonaws.com/%s",
                    awsProperties.getS3().getBucket(),
                    awsProperties.getRegion(),
                    key);

            log.info("#S3UploadServiceImpl.uploadImage: Uploaded image to S3 successfully: {}", url);
            return url;

        } catch (IOException e) {
            log.error("#S3UploadServiceImpl.uploadImage - ERROR - Failed to read input stream from file", e);
            throw new RuntimeException("Error when read image file: " + e.getMessage());
        } catch (Exception e) {
            log.error("#S3UploadServiceImpl.uploadImage - Failed to upload image to S3 due to AWS error", e);
            throw new RuntimeException("AWS ERROR: " + e.getMessage());
        }
    }
}

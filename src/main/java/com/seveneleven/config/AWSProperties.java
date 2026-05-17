package com.seveneleven.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "aws")
public class AWSProperties {

    private String accessKey;

    private String secretKey;

    private String region;

    private boolean enabled = false;

    private S3 s3 = new S3();

    private SES ses = new SES();

    @Getter
    @Setter
    public static class S3 {
        private String bucket;
        private String roleArn;
        private String roleSessionName;
    }

    @Getter
    @Setter
    public static class SES {
        private String roleArn;
        private String roleSessionName;
        private String adminEmail;
        private String senderEmail;
    }
}

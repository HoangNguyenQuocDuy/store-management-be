package com.seveneleven.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

@Configuration
@EnableConfigurationProperties(AWSProperties.class)
@ConditionalOnProperty(prefix = "aws", name = "enabled", havingValue = "true")
public class AWSConfig {

    @Bean
    public S3Client s3Client(AWSProperties props) {
        return S3Client.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(buildCredentialsProvider(props,
                        props.getS3().getRoleArn(), props.getS3().getRoleSessionName()))
                .build();
    }

    @Bean
    public SesV2Client sesV2Client(AWSProperties props) {
        return SesV2Client.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(buildCredentialsProvider(props,
                        props.getSes().getRoleArn(), props.getSes().getRoleSessionName()))
                .build();
    }

    private AwsCredentialsProvider buildCredentialsProvider(AWSProperties props, String roleArn, String sessionName) {
        AwsBasicCredentials base = AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey());

        if (roleArn == null || roleArn.isBlank()) {
            return StaticCredentialsProvider.create(base);
        }

        StsClient sts = StsClient.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(base))
                .build();

        return StsAssumeRoleCredentialsProvider.builder()
                .stsClient(sts)
                .refreshRequest(AssumeRoleRequest.builder()
                        .roleArn(roleArn)
                        .roleSessionName(sessionName)
                        .durationSeconds(3600)
                        .build())
                .build();
    }
}

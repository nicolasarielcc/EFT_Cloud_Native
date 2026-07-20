package com.duoc.transportmanagement.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String accessSecret;

    @Value("${aws.session-token}")
    private String sessionToken;

    @Value("${aws.region}")
    private String region;

    @Bean
    public AmazonS3 s3Client() {
        AWSCredentials credentials = new BasicSessionCredentials(accessKey, accessSecret, sessionToken);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region).build();
    }
}

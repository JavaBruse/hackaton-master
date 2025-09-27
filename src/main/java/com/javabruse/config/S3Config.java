package com.javabruse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${bucket.access-key}")
    private String accessKey;

    @Value("${bucket.secret-access-key}")
    private String secretAccessKey;

    @Value("${bucket.url}")
    private String endpoint;

    @Value("${bucket.name}")
    private String bucketName;

    @Bean
    public S3Presigner s3Presigner() {
        AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretAccessKey)
        );

        S3Configuration s3Config = S3Configuration.builder()
                .pathStyleAccessEnabled(false)
                .chunkedEncodingEnabled(true)
                .build();

        return S3Presigner.builder()
                .region(Region.of("ru-1"))
                .credentialsProvider(credentialsProvider)
                .endpointOverride(URI.create(endpoint))
                .serviceConfiguration(s3Config)
                .build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretAccessKey)
                ))
                .region(Region.of("ru-1"))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(false)
                        .build())
                .build();
    }

    @Bean
    public String s3BaseUrl() {
        return String.format("https://%s.s3.twcstorage.ru/", bucketName);
    }
}
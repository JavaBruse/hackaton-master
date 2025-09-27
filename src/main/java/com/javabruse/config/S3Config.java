package com.javabruse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${backet.access-key}")
    private String accessKey;

    @Value("${backet.secret-access-key}")
    private String secretAccessKey;

    @Value("${backet.url}")
    private String endpoint;

    @Value("${backet.name}")
    private String bucketName;

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretAccessKey)
                ))
                .region(Region.of("ru-1"))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
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
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    @Bean
    public String s3BaseUrl() {
        // Проверяем, поддерживает ли endpoint поддомены
        if (endpoint.contains("s3.twcstorage.ru")) {
            return String.format("https://%s.s3.twcstorage.ru/", bucketName);
        } else {
            return endpoint + "/" + bucketName + "/";
        }
    }
}
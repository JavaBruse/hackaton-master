package com.javabruse.service;

import com.javabruse.model.PresignedUploadResponse;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Service
public class S3PresignedUrlService {

    private final S3Presigner s3Presigner;
    private final String bucketName;

    public S3PresignedUrlService(S3Presigner s3Presigner,
                                 @Value("${bucket.name}") String bucketName) {
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
    }

    public PresignedUploadResponse generatePresignedUploadUrl(UUID photoId, String contentType) {
        String objectKey = String.format("photos/%s%s", photoId, getExtension(contentType));
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(builder ->
                builder.signatureDuration(Duration.ofMinutes(15))
                        .putObjectRequest(putObjectRequest)
        );

        return new PresignedUploadResponse(
                photoId.toString(),
                presignedRequest.url().toString(),
                photoId + getExtension(contentType),
                System.currentTimeMillis() + Duration.ofMinutes(15).toMillis()
        );
    }

    public PresignedUploadResponse generatePresignedUploadUrlWithMetadata(
            UUID photoId, String contentType, long fileSize, String userId) {

        String objectKey = String.format("%s/photos/%s%s",userId, photoId, getExtension(contentType));

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .contentLength(fileSize)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(builder ->
                builder.signatureDuration(Duration.ofMinutes(15))
                        .putObjectRequest(putObjectRequest)
        );

        return new PresignedUploadResponse(
                photoId.toString(),
                presignedRequest.url().toString(),
                photoId + getExtension(contentType),
                System.currentTimeMillis() + Duration.ofMinutes(15).toMillis()
        );
    }

    public String generatePresignedViewUrl(String objectKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(builder ->
                builder.signatureDuration(Duration.ofMinutes(60))
                        .getObjectRequest(getObjectRequest)
        );

        return presignedRequest.url().toString();
    }

    private String getExtension(String contentType) {
        if (contentType == null) return ".bin";
        switch (contentType.toLowerCase()) {
            case "image/jpeg":
            case "image/jpg": return ".jpg";
            case "image/png": return ".png";
            case "image/gif": return ".gif";
            case "image/webp": return ".webp";
            default: return ".bin";
        }
    }

    @PreDestroy
    public void cleanup() {
        if (s3Presigner != null) {
            s3Presigner.close();
        }
    }
}

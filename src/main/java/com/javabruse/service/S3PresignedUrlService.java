package com.javabruse.service;

import com.javabruse.model.PresignedUploadResponse;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
public class S3PresignedUrlService {

    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final String s3BaseUrl;

    public S3PresignedUrlService(S3Presigner s3Presigner,
                                 @Value("${bucket.name}") String bucketName,
                                 String s3BaseUrl) {
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
        this.s3BaseUrl = s3BaseUrl;
    }

    public PresignedUploadResponse generatePresignedUploadUrl(UUID photoId, String contentType) {
        String objectKey = String.format("photos/%s%s", photoId, getExtension(contentType));
        String fullUrl = s3BaseUrl + objectKey;
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
//                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(builder ->
                builder.signatureDuration(Duration.ofMinutes(15))
                        .putObjectRequest(putObjectRequest)
        );

        return new PresignedUploadResponse(
                photoId.toString(),
                presignedRequest.url().toString(), // Presigned URL для загрузки
                fullUrl,
                System.currentTimeMillis() + Duration.ofMinutes(15).toMillis()
        );
    }

    public PresignedUploadResponse generatePresignedUploadUrlWithMetadata(
            UUID photoId, String contentType, long fileSize, String userId) {

        String objectKey = String.format("%s/photos/%s%s",
                userId, photoId, getExtension(contentType));

        String fullUrl = s3BaseUrl + objectKey;

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
                fullUrl,
                System.currentTimeMillis() + Duration.ofMinutes(15).toMillis()
        );
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

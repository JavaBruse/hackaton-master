package com.javabruse.service;

import com.javabruse.model.PresignedUploadResponse;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
        // Генерируем objectKey
        String objectKey = String.format("photos/%s%s", photoId, getExtension(contentType));

        // Полный URL для filePath
        String fullUrl = s3BaseUrl + objectKey;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName) // ⬅️ bucketName используется здесь!
                .key(objectKey)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(builder ->
                builder.signatureDuration(Duration.ofMinutes(15))
                        .putObjectRequest(putObjectRequest)
        );

        return new PresignedUploadResponse(
                photoId.toString(),
                presignedRequest.url().toString(), // Presigned URL для загрузки
                fullUrl, // ⬅️ Полный URL для сохранения в filePath
                System.currentTimeMillis() + Duration.ofMinutes(15).toMillis()
        );
    }

    public PresignedUploadResponse generatePresignedUploadUrlWithMetadata(
            UUID photoId, String contentType, long fileSize, String userId) {

        // ObjectKey с пользовательской папкой
        String objectKey = String.format("users/%s/photos/%s%s",
                userId, photoId, getExtension(contentType));

        // Полный URL для filePath
        String fullUrl = s3BaseUrl + objectKey;

        Map<String, String> metadata = Map.of(
                "uploaded-by", userId,
                "photo-id", photoId.toString(),
                "upload-timestamp", String.valueOf(System.currentTimeMillis())
        );

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName) // ⬅️ bucketName используется здесь!
                .key(objectKey)
                .contentType(contentType)
                .contentLength(fileSize)
                .metadata(metadata)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(builder ->
                builder.signatureDuration(Duration.ofMinutes(15))
                        .putObjectRequest(putObjectRequest)
        );

        return new PresignedUploadResponse(
                photoId.toString(),
                presignedRequest.url().toString(),
                fullUrl, // ⬅️ Полный URL для filePath
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

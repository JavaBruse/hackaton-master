package com.javabruse.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PresignedUploadResponse {
    private String fileId;          // ID для отслеживания
    private String uploadUrl;       // Presigned URL для загрузки
    private String objectKey;       // Ключ в S3 (для дальнейшего использования)
    private long expiresAt;         // Время истечения


    public PresignedUploadResponse(String fileId, String uploadUrl,
                                   String objectKey, long expiresAt) {
        this.fileId = fileId;
        this.uploadUrl = uploadUrl;
        this.objectKey = objectKey;
        this.expiresAt = expiresAt;
    }
}

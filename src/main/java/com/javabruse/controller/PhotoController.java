package com.javabruse.controller;

import com.javabruse.DTO.PhotoRequest;
import com.javabruse.DTO.PhotoResponse;
import com.javabruse.model.PresignedUploadResponse;
import com.javabruse.service.PhotoService;
import com.javabruse.service.S3PresignedUrlService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/photo")
public class PhotoController {
    private final PhotoService photoService;
    private final S3PresignedUrlService presignedUrlService;

    @Operation(summary = "Получить PresignedUploadResponse для загрузки фото на S3, 50мб < фотографии загрузить нельзя. ")
    @PostMapping("/upload")
    public ResponseEntity<PresignedUploadResponse> initUpload(@RequestBody PhotoRequest photoRequest, HttpServletRequest request) {
        UUID userUUID = UUID.fromString(request.getHeader("X-User-Id"));
        // Генерируем ID для фото
        photoRequest.setId(UUID.randomUUID());
        try {
            // Валидация
            if (photoRequest.getFileSize() != null && photoRequest.getFileSize() > 50 * 1024 * 1024) {
                return ResponseEntity.badRequest().build();
            }
            // Генерация Presigned URL
            PresignedUploadResponse response;
            if (photoRequest.getFileSize() != null) {
                response = presignedUrlService.generatePresignedUploadUrlWithMetadata(
                        photoRequest.getId(),
                        photoRequest.getContentType(),
                        photoRequest.getFileSize(),
                        userUUID.toString()
                );
            } else {
                response = presignedUrlService.generatePresignedUploadUrl(
                        photoRequest.getId(),
                        photoRequest.getContentType()
                );
            }
            photoRequest.setFilePath(response.getObjectKey());

            log.info("Запрос на сохранение: "+photoRequest);
            try {
                photoService.add(photoRequest, userUUID);
            } catch (Exception e){
                log.info("Ошибка сохранения: " + e.getMessage());
            }
            System.out.println("=== UPLOAD DEBUG ===");
            System.out.println("Photo saved to DB with filePath: " + photoRequest.getFilePath());
            System.out.println("Presigned URL generated: " + response.getUploadUrl());
            System.out.println("=== END DEBUG ===");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Получить все фотографии пользователя")
    @GetMapping("/all")
    public ResponseEntity<List<PhotoResponse>> getAll(HttpServletRequest request) {
        UUID userUUID = UUID.fromString(request.getHeader("X-User-Id"));
        try {
            return ResponseEntity.status(HttpStatus.OK).body(photoService.getAll(userUUID));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Operation(summary = "Получить все фотографии по задаче пользователя")
    @GetMapping("/all/{id}")
    public ResponseEntity<List<PhotoResponse>> getAllByTask(@PathVariable String id, HttpServletRequest request) {
        UUID userUUID = UUID.fromString(request.getHeader("X-User-Id"));
        try {
            return ResponseEntity.status(HttpStatus.OK).body(photoService.getAllByTask(UUID.fromString(id), userUUID));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    @Operation(summary = "Обновляет фото")
    @PostMapping("/save")
    public ResponseEntity<List<PhotoResponse>> save(@RequestBody PhotoRequest photo, HttpServletRequest request) {
        UUID userUUID = UUID.fromString(request.getHeader("X-User-Id"));
        try {
            return ResponseEntity.status(HttpStatus.OK).body(photoService.update(photo, userUUID));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Operation(summary = "Удаляет фото по ID")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<List<PhotoResponse>> deleteFromIdByUUID(@PathVariable String id, HttpServletRequest request) {
        UUID userUUID = UUID.fromString(request.getHeader("X-User-Id"));
        try {
            return ResponseEntity.status(HttpStatus.OK).body(photoService.delete(UUID.fromString(id), userUUID));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Operation(summary = "Получить PhotoResponse по ID фото")
    @GetMapping("/{id}")
    public ResponseEntity<PhotoResponse> getPhotoUrlByID(@PathVariable String id, HttpServletRequest request) {
        UUID userUUID = UUID.fromString(request.getHeader("X-User-Id"));
        try {
            return ResponseEntity.status(HttpStatus.OK).body(photoService.getPhoto(UUID.fromString(id), userUUID));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Operation(summary = "Redirect на фото в базе данных")
    @GetMapping("/view/{id}")
    public ResponseEntity<?> getViewPhotoUrlByID(@PathVariable String id, HttpServletRequest request) {
        UUID userUUID = UUID.fromString(request.getHeader("X-User-Id"));
        try {
            String filePath = photoService.getPhoto(UUID.fromString(id), userUUID).getFilePath();
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(filePath))
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}

package com.javabruse.controller;

import com.javabruse.DTO.PhotoRequest;
import com.javabruse.DTO.PhotoResponse;
import com.javabruse.DTO.TaskRequest;
import com.javabruse.DTO.TaskResponse;
import com.javabruse.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

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


    @Operation(summary = "Добавляет новое фото")
    @PostMapping("/add")
    public ResponseEntity<List<PhotoResponse>> add(@RequestBody PhotoRequest photo, HttpServletRequest request) {
        UUID userUUID = UUID.fromString(request.getHeader("X-User-Id"));
        try {
            return ResponseEntity.status(HttpStatus.OK).body(photoService.add(photo, userUUID));
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
}

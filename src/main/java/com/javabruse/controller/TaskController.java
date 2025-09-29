package com.javabruse.controller;

import com.javabruse.DTO.*;
import com.javabruse.model.Task;
import com.javabruse.service.TaskService;
import com.javabruse.service.kafka.KafkaProducerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/task")
public class TaskController {

    private final KafkaProducerService kafkaProducerService;
    private static final Logger log = LoggerFactory.getLogger(TaskController.class);
    private final TaskService taskService;

    @Operation(summary = "Получить все задачи пользователя")
    @GetMapping("/all")
    public ResponseEntity<List<TaskResponse>> getAll(HttpServletRequest request) {
        UUID userUUID = UUID.fromString(request.getHeader("X-User-Id"));
        try {
            return ResponseEntity.status(HttpStatus.OK).body(taskService.getAll(userUUID));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    @Operation(summary = "Добавляет новую задачу")
    @PostMapping("/add")
    public ResponseEntity<List<TaskResponse>> add(@RequestBody TaskRequest task, HttpServletRequest request) {
        UUID userUUID = UUID.fromString(request.getHeader("X-User-Id"));
        try {
            return ResponseEntity.status(HttpStatus.OK).body(taskService.add(task, userUUID));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Operation(summary = "Обновляет новую задачу")
    @PostMapping("/save")
    public ResponseEntity<List<TaskResponse>> save(@RequestBody TaskRequest task, HttpServletRequest request) {
        UUID userUUID = UUID.fromString(request.getHeader("X-User-Id"));
        try {
            return ResponseEntity.status(HttpStatus.OK).body(taskService.update(task, userUUID));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Operation(summary = "Удаляет задачу по ID")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<List<TaskResponse>> deleteFromIdByUUID(@PathVariable String id, HttpServletRequest request) {
        UUID userUUID = UUID.fromString(request.getHeader("X-User-Id"));
        try {
            return ResponseEntity.status(HttpStatus.OK).body(taskService.delete(UUID.fromString(id), userUUID));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    @Operation(summary = "Запустить задачу")
    @PutMapping("/start/{id}")
    public  ResponseEntity<List<TaskResponse>> startTask(@PathVariable String id, HttpServletRequest request){
        UUID userUUID = UUID.fromString(request.getHeader("X-User-Id"));
        try {
            return ResponseEntity.status(HttpStatus.OK).body(taskService.sendTaskToKafka(UUID.fromString(id), userUUID));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}

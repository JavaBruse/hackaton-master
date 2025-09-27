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





















//    @GetMapping("/send-task")
//    public ResponseEntity<Object> getAllPostBySourceID(HttpServletRequest request) {
//        UUID userUUID = UUID.fromString(request.getHeader("X-User-Id"));
//        log.info("UUID пользователя: " + userUUID);
//        try {
//            TaskMessage message = new TaskMessage();
//            message.setId(UUID.randomUUID());
//            message.setTaskID(UUID.randomUUID());
//            PhotoMessage photoMessage = new PhotoMessage();
//            CamMessage camMessage = new CamMessage();
//            camMessage.setElevation(4156d);
//            camMessage.setId(UUID.randomUUID());
//            camMessage.setBearing(4564d);
//            camMessage.setLongitude(45615646d);
//            camMessage.setLatitude(3123412d);
//            camMessage.setAddress("Адрес камеры");
//            photoMessage.setFilePath("/3123/5125/fweg/213/Какой то путь/");
//            photoMessage.setId(UUID.randomUUID());
//            List<ConstructionMessage> list = new ArrayList<>();
//            for (int i = 0; i < 5; i++) {
//                ConstructionMessage constructionMessage = new ConstructionMessage();
//                constructionMessage.setLongitude(Double.valueOf(((i + 12) * 5161)));
//                constructionMessage.setLatitude(Double.valueOf(((i + 32) * 3123)));
//                constructionMessage.setAddress("Адресс" + i + 2);
//                list.add(constructionMessage);
//            }
//            photoMessage.setConstructionMessageList(list);
//            photoMessage.setCamMessage(camMessage);
//            message.setPhotoMessage(photoMessage);
//            kafkaProducerService.sendTransferRequestTask(message);
//            return ResponseEntity.status(HttpStatus.OK).body("Отправлено!");
//        } catch (Exception e) {
//            log.error("/v1/task/send-task    Error -----------------------------" + e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        }
//    }
}

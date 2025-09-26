package com.javabruse.controller;

import com.javabruse.DTO.PhotoTaskDTO;
import com.javabruse.service.kafka.KafkaProducerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/task")
public class TaskController {

    private final KafkaProducerService kafkaProducerService;
    private static final Logger log = LoggerFactory.getLogger(TaskController.class);


    @GetMapping("/send-task")
    public ResponseEntity<Object> getAllPostBySourceID(HttpServletRequest request) {
        UUID userUUID = UUID.fromString(request.getHeader("X-User-Id"));
        log.info("UUID пользователя: " + userUUID);
        try {
            PhotoTaskDTO photoTaskDTO = new PhotoTaskDTO();
            photoTaskDTO.setPhotoID(UUID.randomUUID());
            photoTaskDTO.setTaskID(UUID.randomUUID());
            photoTaskDTO.setLatitude(3131321d);
            photoTaskDTO.setLongitude(312312312d);
            photoTaskDTO.setAddress("gkwegkjewgkljweg");
            photoTaskDTO.setId(UUID.randomUUID());
            photoTaskDTO.setPhotoID(UUID.randomUUID());
            photoTaskDTO.setFilePath("/efwfwe/ewfewfwe/gewghew/hthjrt/");
            kafkaProducerService.sendTransferRequestTask(photoTaskDTO);
            return ResponseEntity.status(HttpStatus.OK).body("Отправлено!");
        } catch (Exception e) {
            log.error("/v1/task/send-task    Error -----------------------------" + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

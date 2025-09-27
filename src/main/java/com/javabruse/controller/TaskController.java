package com.javabruse.controller;

import com.javabruse.DTO.CamMessage;
import com.javabruse.DTO.ConstructionMessage;
import com.javabruse.DTO.PhotoMessage;
import com.javabruse.DTO.TaskMessage;
import com.javabruse.service.kafka.KafkaProducerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
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
            TaskMessage message = new TaskMessage();
            message.setId(UUID.randomUUID());
            message.setTaskID(UUID.randomUUID());
            PhotoMessage photoMessage = new PhotoMessage();
            CamMessage camMessage = new CamMessage();
            photoMessage.setFilePath("///Какой то путь///");
            photoMessage.setId(UUID.randomUUID());
            List<ConstructionMessage> list = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                ConstructionMessage constructionMessage = new ConstructionMessage();
                constructionMessage.setLongitude(Double.valueOf(((i + 12) * 5161)));
                constructionMessage.setLatitude(Double.valueOf(((i + 32) * 3123)));
                constructionMessage.setAddress("Адресс" + i + 2);
                list.add(constructionMessage);
            }
            photoMessage.setConstructionMessageList(list);
            photoMessage.setCamMessage(camMessage);
            message.setPhotoMessage(photoMessage);
            kafkaProducerService.sendTransferRequestTask(message);
            return ResponseEntity.status(HttpStatus.OK).body("Отправлено!");
        } catch (Exception e) {
            log.error("/v1/task/send-task    Error -----------------------------" + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

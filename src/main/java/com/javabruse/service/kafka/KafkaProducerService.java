package com.javabruse.service.kafka;

import com.javabruse.DTO.PhotoTaskDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, PhotoTaskDTO> kafkaTemplateTask;
    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    @Value("${topics.transfer-requests-task}")
    private String requestTask;


    public void sendTransferRequestTask(PhotoTaskDTO request) {
        kafkaTemplateTask.send(requestTask, request.getId().toString(), request);
        log.info("Отправил сообщение в кафку на: " + requestTask + " объект: " + request);
    }

}


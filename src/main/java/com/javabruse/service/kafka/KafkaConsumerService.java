package com.javabruse.service.kafka;//package com.Trochilidae.core.services.kafka;

import com.javabruse.DTO.TaskMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Value("${topics.transfer-response-task}")
    private String responseTaskTopic;

    @Value("${topics.transfer-requests-task}")
    private String requestTaskTopic;

    @KafkaListener(topics = "${topics.transfer-response-task}", groupId = "MASTER_SERVICE")
    public void listenTransferResponse(TaskMessage request) {
        log.info("Получил сообщение из кафки топик: " + responseTaskTopic + " объект: " + request);
    }


    @KafkaListener(topics = "${topics.transfer-requests-task}", groupId = "MASTER_SERVICE")
    public void listenTransferRequest(TaskMessage request) {
        log.info("Получил сообщение из кафки топик: " + requestTaskTopic + " объект: " + request);
    }
}
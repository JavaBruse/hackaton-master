package com.javabruse.service.kafka;

import com.javabruse.DTO.TaskMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, TaskMessage> kafkaTemplateTask;
    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    @Value("${topics.transfer-requests-task}")
    private String requestTaskTopic;


    public void sendTransferRequestTask(TaskMessage request) {
        kafkaTemplateTask.send(requestTaskTopic, request.getId().toString(), request);
        log.info("Отправил сообщение в кафку на: " + requestTaskTopic + " объект: " + request);
    }

}


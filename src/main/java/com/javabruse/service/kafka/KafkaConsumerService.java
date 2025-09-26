package com.javabruse.service.kafka;//package com.Trochilidae.core.services.kafka;

import com.javabruse.DTO.PhotoTaskDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @Value("${topics.transfer-response-task}")
    private String responseTaskTopic;

    @KafkaListener(topics = "${topics.transfer-response-task}", groupId = "MASTER_SERVICE")
    public void listenTransferRequest(PhotoTaskDTO request) {
        System.out.println("Получено сообщение: " + request);
    }
}
package com.javabruse.service.kafka;//package com.Trochilidae.core.services.kafka;

import com.javabruse.DTO.ConstructionMessage;
import com.javabruse.DTO.TaskMessage;
import com.javabruse.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);
    private final TaskService taskService;
    private final KafkaProducerService kafkaProducerService;


    @Value("${topics.transfer-response-task}")
    private String responseTaskTopic;

    @Value("${topics.transfer-requests-task}")
    private String requestTaskTopic;

    @KafkaListener(topics = "${topics.transfer-response-task}", groupId = "MASTER_SERVICE")
    public void listenTransferResponse(TaskMessage request) {
        taskService.listenTaskFromKafka(request);
        log.info("Получил сообщение из кафки listenTransferResponse  топик: " + responseTaskTopic + " объект: " + request);
    }


    @KafkaListener(topics = "${topics.transfer-requests-task}", groupId = "MASTER_SERVICE")
    public void listenTransferRequest(TaskMessage request) {
        log.info("Получил сообщение из кафки  listenTransferRequest топик: " + responseTaskTopic + " объект: " + request);

        //какие то тестовые данные..
//        55.650937, 37.418443
        request.getPhotoMessage().setFilePathComplete(request.getPhotoMessage().getFilePathOriginal());
        request.getPhotoMessage().getCamMessage().setLatitude(55.650937 +  0.0002); // ±0.001 = ~100 метров
        request.getPhotoMessage().getCamMessage().setLongitude(37.418443 - 0.0002); // Долгота 37-47
        request.getPhotoMessage().getCamMessage().setBearing(Math.random() * 360); // Азимут 0-360
        request.getPhotoMessage().getCamMessage().setElevation(100 + Math.random() * 200); // Высота 100-300
        // Заполняем ConstructionMessage список (3 случайных конструкции)
        List<ConstructionMessage> constructionList = new ArrayList<>();
        String[] types = {"Здание", "Мост", "Дорога", "Тоннель", "Опора"};
        for (int i = 1; i <= 3; i++) {
            ConstructionMessage construction = new ConstructionMessage();
            construction.setPosition(i);
            construction.setType(types[(int)(Math.random() * types.length)]);
            construction.setLatitude(request.getPhotoMessage().getCamMessage().getLatitude() + (Math.random() - 0.05) * 0.01); // ±0.005 от камеры
            construction.setLongitude(request.getPhotoMessage().getCamMessage().getLongitude() + (Math.random() - 0.05) * 0.01); // ±0.005 от камеры
            constructionList.add(construction);
        }
        request.getPhotoMessage().setConstructionMessageList(constructionList);
        kafkaProducerService.sendTransferResponseTask(request);
        log.info("Получил сообщение из кафки топик: " + requestTaskTopic + " объект: " + request);
    }
}